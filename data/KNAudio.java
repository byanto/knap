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
 *   Apr 3, 2016 (budiyanto): created
 */
package org.knime.base.node.audio.data;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.knime.base.node.audio.data.feature.FeatureType;
import org.knime.base.node.audio.data.recognizer.RecognitionResult;
import org.knime.base.node.audio.data.recognizer.RecognizerInfo;
import org.knime.base.node.audio.ext.org.openimaj.audio.AudioFormat;
import org.knime.base.node.audio.ext.org.openimaj.audio.AudioStream;
import org.knime.base.node.audio.ext.org.openimaj.audio.SampleChunk;
import org.knime.base.node.audio.ext.org.openimaj.feature.DoubleFV;
import org.knime.base.node.audio.util.AudioUtils;
import org.knime.core.util.UniqueNameGenerator;

/**
 *
 * @author Budi Yanto, KNIME.com
 */
public class KNAudio extends AudioStream {

    private static final long serialVersionUID = 361102768841852282L;

//    private static NodeLogger LOGGER = NodeLogger.getLogger(KNAudio.class);

    private final String m_name;
    private final String m_filePath;
    private final Map<FeatureType, DoubleFV> m_features;
    private final Map<String, RecognitionResult> m_recognitionResults;
//    private final Map<String, Recognizer> m_recognizers;

    private AudioInputStream m_inStream;

    /**
     *
     * @param url
     */
    public KNAudio(final URL url){
        this(new File(url.toString()));
    }

    /**
     * @param filePath
     *
     */
    public KNAudio(final String filePath){
        this(new File(filePath));
    }

    /**
     *
     * @param file
     */
    public KNAudio(final File file){
        if(file == null){
            throw new IllegalArgumentException("The input file can't be null.");
        }

        if(!file.exists()){
            throw new IllegalArgumentException("File \"" + file.getName() + "\" doesn't exist.");
        }

        if(!file.isFile()){
            throw new IllegalArgumentException("The input \"" + file.getName() + "\" isn't a file.");
        }

        m_name = file.getName();
        m_filePath = file.getAbsolutePath();
        m_features = new LinkedHashMap<FeatureType, DoubleFV>();
//        m_recognizers = new LinkedHashMap<String, Recognizer>();
        m_recognitionResults = new LinkedHashMap<String, RecognitionResult>();
        initialize(file);
    }

    /**
     * @throws IOException
     */
    private void initialize(final File file){
        // Initialize AudioFormat
        try{
            final javax.sound.sampled.AudioFormat audioFormat = AudioSystem.getAudioFileFormat(file).getFormat();
            final AudioFormat fmt = new AudioFormat(
                audioFormat.getSampleSizeInBits(),
                audioFormat.getSampleRate() / 1000,
                audioFormat.getChannels());
            fmt.setBigEndian(audioFormat.isBigEndian());
            if(audioFormat.getEncoding() == Encoding.PCM_SIGNED){
                fmt.setSigned(true);
            }else if(audioFormat.getEncoding() == Encoding.PCM_UNSIGNED){
                fmt.setSigned(false);
            }
            super.setFormat(fmt);
        } catch(UnsupportedAudioFileException ex){
            ex.printStackTrace(); //TODO
        } catch(IOException ex){
            ex.printStackTrace(); //TODO
        }

        // Initialize available Feature Types
        for(FeatureType type : FeatureType.values()){
            m_features.put(type, null);
        }

    }

    /**
     * @return the name
     */
    public String getName() {
        return m_name;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return m_filePath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleChunk nextSampleChunk() {
        openStream();
        final int normalBytes = AudioUtils.normalizeBytesFromBits(getFormat().getNBits());
        byte[] bytes = new byte[AudioUtils.DEF_BUFFER_SAMPLE_SZ * getFormat().getNumChannels() * normalBytes];
//        LOGGER.debug("Next Sample Chunk Bytes Size: " + bytes.length);
        SampleChunk samples = null;
        try{
            final int bread = m_inStream.read(bytes);
            if(bread != -1){
                if(bytes.length == bread){
                    samples = new SampleChunk(bytes, getFormat());
                }else{
                    final byte[] copy = new byte[bread];
                    System.arraycopy(bytes, 0, copy, 0, bread);
                    samples = new SampleChunk(copy, getFormat());
                }
            }

        }catch(IOException ex){
            ex.printStackTrace();
        }

        return samples;
    }

    private void openStream(){
        if(m_inStream == null){
            try{
                m_inStream = AudioSystem.getAudioInputStream(new File(m_filePath));
            } catch(IOException ex){
                ex.printStackTrace();
            } catch(UnsupportedAudioFileException ex){
                ex.printStackTrace();
            }
        }
    }

    private void closeStream(){
        if(m_inStream != null){
            try{
                m_inStream.close();
                m_inStream = null;
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        closeStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLength() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     *
     * @param type
     * @return the feature vector of the given feature type
     */
    public DoubleFV getFeatureVector(final FeatureType type){
        return m_features.get(type);
    }

    /**
     *
     * @param type
     * @param vector
     */
    public void setFeatureVector(final FeatureType type, final DoubleFV vector){
        m_features.put(type, vector);
    }

    /**
     * Returns all features and their already extracted features.
     * @return all features and their already extracted features
     */
    public Map<FeatureType, DoubleFV> getExtractedFeatures(){
        final Map<FeatureType, DoubleFV> result = new LinkedHashMap<FeatureType, DoubleFV>();
        for(Entry<FeatureType, DoubleFV> entry : m_features.entrySet()){
            if(entry.getValue() != null){
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * @return the recognizers
     */
    public Map<String, RecognitionResult> getRecognitionResults() {
        return m_recognitionResults;
    }

    /**
     * @param key the key of the recognition result
     * @return the recognition result based on the given key
     */
    public RecognitionResult getRecognitionResult(final String key){
        return m_recognitionResults.get(key);
    }

    /**
     * Adds a recognition result to the list
     * @param result
     */
    public void addRecognitionResult(final RecognitionResult result){
        if(result == null){
            throw new IllegalArgumentException("result cannot be null");
        }

        final String key = new UniqueNameGenerator(m_recognitionResults.keySet())
                .newName(result.getRecognizerInfo(RecognizerInfo.KEY_NAME).toString());
        m_recognitionResults.put(key, result);
    }

    /**
     * @return <code>true</code> if the audio has recognition result attached to it,
     * otherwise <code>false</code>
     */
    public boolean hasRecognitionResult(){
        return m_recognitionResults != null && !m_recognitionResults.isEmpty();
    }

    @Override
    public KNAudio clone(){
        final KNAudio newAudio = new KNAudio(getFilePath());
        for(Entry<FeatureType, DoubleFV> entry : getExtractedFeatures().entrySet()){
            newAudio.setFeatureVector(entry.getKey(), entry.getValue());
        }
        for(Entry<String, RecognitionResult> entry : getRecognitionResults().entrySet()){
            newAudio.getRecognitionResults().put(entry.getKey(), entry.getValue());
        }

        return newAudio;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_features == null) ? 0 : m_features.hashCode());
        result = prime * result + ((m_filePath == null) ? 0 : m_filePath.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        KNAudio other = (KNAudio)obj;
        if (m_features == null) {
            if (other.m_features != null) {
                return false;
            }
        } else if (!m_features.equals(other.m_features)) {
            return false;
        }
        if (m_filePath == null) {
            if (other.m_filePath != null) {
                return false;
            }
        } else if (!m_filePath.equals(other.m_filePath)) {
            return false;
        }
        return true;
    }



}
