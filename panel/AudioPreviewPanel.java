/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Mar 15, 2016 (budiyanto): created
 */
package org.knime.base.node.audio.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import org.knime.base.node.audio.util.AudioUtils;
import org.knime.core.node.NodeLogger;

/**
 * The code is mainly based on
 * https://github.com/Radiodef/WaveformDemo/blob/master/waveformdemo/WaveformDemo.java
 *
 * Original author: David Staver, 2013
 *
 * This work is licensed under the Creative Commons
 * Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/3.0/
 *
 * @author Budi Yanto, KNIME.com
 */
public class AudioPreviewPanel extends JPanel{

    private static NodeLogger LOGGER = NodeLogger.getLogger(AudioPreviewPanel.class);

    private static final Color LIGHT_BLUE = new Color(128, 192, 255);
    private static final Color DARK_BLUE = new Color(0, 0, 127);

    private enum PlayStatus {
        NO_FILE, PLAYING, PAUSED, STOPPED
    }

    public interface PlayerRef {
        public Object getLock();

        public PlayStatus getStat();

        public File getFile();

        public void playbackEnded();

        public void drawDisplay(float[] samples, int svalid);
    }

    private JPanel m_contentPane = new JPanel(new BorderLayout());

    private JLabel m_fileLabel = new JLabel("No file loaded");

    private DisplayPanel m_displayPanel = new DisplayPanel();

    private JToolBar m_playbackTools = new JToolBar();

    private JButton m_buttonPlay = new JButton("Play");

    private JButton m_buttonPause = new JButton("Pause");

    private JButton m_buttonStop = new JButton("Stop");

    private File m_audioFile;

    private AudioFormat m_audioFormat;

    private final Object m_statusLock = new Object();

    private volatile PlayStatus m_playStatus = PlayStatus.NO_FILE;

    private final PlayerRef thisPlayer = new PlayerRef() {
        @Override
        public Object getLock() {
            return m_statusLock;
        }

        @Override
        public PlayStatus getStat() {
            return m_playStatus;
        }

        @Override
        public File getFile() {
            return m_audioFile;
        }

        @Override
        public void playbackEnded() {
            synchronized (m_statusLock) {
                m_playStatus = PlayStatus.STOPPED;
            }
            m_displayPanel.reset();
            m_displayPanel.repaint();
        }

        @Override
        public void drawDisplay(final float[] samples, final int svalid) {
            m_displayPanel.makePath(samples, svalid);
            m_displayPanel.repaint();
        }
    };

    public AudioPreviewPanel(){
        m_playbackTools.setLayout(new FlowLayout());
        m_playbackTools.setFloatable(false);
        m_playbackTools.add(m_buttonPlay);
        m_playbackTools.add(m_buttonPause);
        m_playbackTools.add(m_buttonStop);

        m_buttonPlay.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                onPlay();
            }
        });
        m_buttonPause.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                onPause();
            }
        });
        m_buttonStop.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(final ActionEvent e) {
                onStop();
            }
        });

        m_fileLabel.setOpaque(true);
        m_fileLabel.setBackground(Color.BLACK);
        m_fileLabel.setForeground(Color.WHITE);
        m_fileLabel.setHorizontalAlignment(SwingConstants.CENTER);

        m_playbackTools.setBackground(Color.GRAY);
        m_playbackTools.setMargin(new Insets(0, 24, 0, 0));

        m_contentPane.add(m_fileLabel, BorderLayout.NORTH);
        m_contentPane.add(m_displayPanel, BorderLayout.CENTER);
        m_contentPane.add(m_playbackTools, BorderLayout.SOUTH);

        add(m_contentPane);
    }

    public void onLoad(final File selected){

        synchronized (m_statusLock) {
            if (m_playStatus == PlayStatus.PLAYING) {
                m_playStatus = PlayStatus.STOPPED;
            }
        }

        try {

            /*
             * first test to see if format supported.
             * sometimes the system will claim to support a format
             * but throw a LineUnavailableException on SourceDataLine.open
             *
             * if retrieving a list of DataLine.Info for available
             * supported formats some systems return -1 for sample rates
             * indicating 'any' but evidently can be untrue: throws the exception.
             *
             */

            AudioFileFormat fmt = AudioSystem.getAudioFileFormat(selected);

            m_audioFile = selected;
            m_audioFormat = fmt.getFormat();
            m_fileLabel.setText(m_audioFile.getName());
            m_playStatus = PlayStatus.STOPPED;

        } catch (IOException ioe) {
            AudioUtils.showError(ioe);
        } catch (UnsupportedAudioFileException uafe) {
            AudioUtils.showError(uafe);
        }
    }

    public void onPlay(){
        if(m_audioFile != null && m_playStatus != PlayStatus.PLAYING){
            synchronized (m_statusLock) {
                switch (m_playStatus) {

                    case STOPPED: {
                        m_playStatus = PlayStatus.PLAYING;
                        new PlaybackLoop(thisPlayer).execute();
                        break;
                    }

                    case PAUSED: {
                        m_playStatus = PlayStatus.PLAYING;
                        m_statusLock.notifyAll();
                        break;
                    }
                }
            }
        }
    }

    public void onPause(){
        if(m_playStatus == PlayStatus.PLAYING){
            synchronized (m_statusLock) {
                m_playStatus = PlayStatus.PAUSED;
            }
        }
    }

    public void onStop(){
        if(m_playStatus == PlayStatus.PLAYING || m_playStatus == PlayStatus.PAUSED){
            synchronized (m_statusLock) {
                switch (m_playStatus) {

                    case PAUSED: {
                        m_playStatus = PlayStatus.STOPPED;
                        m_statusLock.notifyAll();
                        break;
                    }

                    case PLAYING: {
                        m_playStatus = PlayStatus.STOPPED;
                        break;
                    }
                }
            }
        }
    }

    private static class PlaybackLoop extends SwingWorker<Void, Void> {

        private final PlayerRef playerRef;

        private PlaybackLoop(final PlayerRef pr) {
            playerRef = pr;
        }

        @Override
        public Void doInBackground() {
            try {
                AudioInputStream in = null;
                SourceDataLine out = null;

                try {
                    try {
                        final AudioFormat audioFormat =
                            (AudioSystem.getAudioFileFormat(playerRef.getFile()).getFormat());

                        in = AudioSystem.getAudioInputStream(playerRef.getFile());
                        out = AudioSystem.getSourceDataLine(audioFormat);

//                        AudioFormat fmt = in.getFormat();
//                        LOGGER.debug("== Original Audio ==");
//                        LOGGER.debug("Frame Length: " + in.getFrameLength());
//                        LOGGER.debug("--- Audio Format Original ---");
//                        LOGGER.debug("ToString: " + fmt.toString());
//                        LOGGER.debug("Channels: " + fmt.getChannels());
//                        LOGGER.debug("Encoding: " + fmt.getEncoding());
//                        LOGGER.debug("Frame Rate: " + fmt.getFrameRate());
//                        LOGGER.debug("Frame Size: " + fmt.getFrameSize());
//                        LOGGER.debug("Sample Rate: " + fmt.getSampleRate());
//                        LOGGER.debug("Sample size in Bits: " + fmt.getSampleSizeInBits());
//                        LOGGER.debug("Is Big Endian: " + fmt.isBigEndian());


                        final int normalBytes = AudioUtils.normalizeBytesFromBits(audioFormat.getSampleSizeInBits());

                        float[] samples = new float[AudioUtils.DEF_BUFFER_SAMPLE_SZ * audioFormat.getChannels()];
                        long[] transfer = new long[samples.length];
                        byte[] bytes = new byte[samples.length * normalBytes];

                        out.open(audioFormat, bytes.length);
                        out.start();

                        /*
                         * feed the output some zero samples
                         * helps prevent the 'stutter' issue.
                         *
                         */

                        for (int feed = 0; feed < 6; feed++) {
                            out.write(bytes, 0, bytes.length);
                        }

                        int bread;

                        play_loop: do {
                            while (playerRef.getStat() == PlayStatus.PLAYING) {

                                if ((bread = in.read(bytes)) == -1) {

                                    break play_loop; // eof
                                }

                                samples = AudioUtils.unpack(bytes, transfer, samples, bread, audioFormat);
                                samples = AudioUtils.window(samples, bread / normalBytes, audioFormat);

                                playerRef.drawDisplay(samples, bread / normalBytes);

                                out.write(bytes, 0, bread);
                            }

                            if (playerRef.getStat() == PlayStatus.PAUSED) {
                                out.flush();
                                try {
                                    synchronized (playerRef.getLock()) {
                                        playerRef.getLock().wait(1000L);
                                    }
                                } catch (InterruptedException ie) {
                                }
                                continue;
                            } else {
                                break;
                            }
                        } while (true);

                    } catch (UnsupportedAudioFileException uafe) {
                        AudioUtils.showError(uafe);
                    } catch (LineUnavailableException lue) {
                        AudioUtils.showError(lue);
                    }
                } finally {
                    if (in != null) {
                        in.close();
                    }
                    if (out != null) {
                        out.flush();
                        out.close();
                    }
                }
            } catch (IOException ioe) {
                AudioUtils.showError(ioe);
            }

            return null;
        }

        @Override
        public void done() {
            playerRef.playbackEnded();

            try {
                get();
            } catch (InterruptedException io) {
            } catch (CancellationException ce) {
            } catch (ExecutionException ee) {
                AudioUtils.showError(ee.getCause());
            }
        }
    }

    private class DisplayPanel extends JPanel {

        private final BufferedImage image;

        private final Path2D.Float[] paths = {new Path2D.Float(), new Path2D.Float(), new Path2D.Float()};

        private final Object pathLock = new Object();

        private DisplayPanel() {
            Dimension pref = getPreferredSize();

            image = (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                .getDefaultConfiguration().createCompatibleImage(pref.width, pref.height, Transparency.OPAQUE));
            setOpaque(false);
        }

        private void reset() {
            Graphics2D g2d = image.createGraphics();
            g2d.setBackground(Color.BLACK);
            g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
            g2d.dispose();
        }

        public void makePath(final float[] samples, final int svalid) {
            if (m_audioFormat == null) {
                return;
            }

            /* shuffle */

            Path2D.Float current = paths[2];
            paths[2] = paths[1];
            paths[1] = paths[0];

            /* lots of ratios */

            float avg = 0f;
            float hd2 = getHeight() / 2f;

            final int channels = m_audioFormat.getChannels();

            /*
             * have to do a special op for the
             * 0th samples because moveTo.
             *
             */

            int i = 0;
            while (i < channels && i < svalid) {
                avg += samples[i++];
            }

            avg /= channels;

            current.reset();
            current.moveTo(0, hd2 - avg * hd2);

            int fvalid = svalid / channels;
            for (int ch, frame = 0; i < svalid; frame++) {
                avg = 0f;

                /* average the channels for each frame. */

                for (ch = 0; ch < channels; ch++) {
                    avg += samples[i++];
                }

                avg /= channels;

                current.lineTo((float)frame / fvalid * image.getWidth(), hd2 - avg * hd2);
            }

            paths[0] = current;

            Graphics2D g2d = image.createGraphics();

            synchronized (pathLock) {
                g2d.setBackground(Color.BLACK);
                g2d.clearRect(0, 0, image.getWidth(), image.getHeight());

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

                g2d.setPaint(DARK_BLUE);
                g2d.draw(paths[2]);

                g2d.setPaint(LIGHT_BLUE);
                g2d.draw(paths[1]);

                g2d.setPaint(Color.WHITE);
                g2d.draw(paths[0]);
            }

            g2d.dispose();
        }

        @Override
        protected void paintComponent(final Graphics g) {
            super.paintComponent(g);

            synchronized (pathLock) {
                g.drawImage(image, 0, 0, null);
            }
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(AudioUtils.DEF_BUFFER_SAMPLE_SZ / 2, 128);
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }
    }

}
