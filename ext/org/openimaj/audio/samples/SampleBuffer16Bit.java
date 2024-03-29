/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 *
 */
package org.knime.base.node.audio.ext.org.openimaj.audio.samples;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Iterator;

import org.apache.commons.lang.NotImplementedException;
import org.knime.base.node.audio.ext.org.openimaj.audio.AudioFormat;
import org.knime.base.node.audio.ext.org.openimaj.audio.SampleChunk;
import org.knime.base.node.audio.ext.org.openimaj.audio.timecode.AudioTimecode;

/**
 * 	A {@link SampleBuffer} for 16-bit sample chunks.
 *
 * 	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *	@created 23rd November 2011
 */
public class SampleBuffer16Bit implements SampleBuffer, Iterator<Float>
{
	/** The underlying byte array we're wrapping */
	private byte[] samples = null;

	/** The short buffer that we're wrapping */
	private ShortBuffer shortBuffer = null;

	/** The audio format of the samples */
	private AudioFormat format;

	/** A counter used for iterating over the samples */
	private int iteratorCount;

	/** The timecode of this buffer */
	private AudioTimecode timecode = null;

	/**
	 * 	Create a new 16-bit sample buffer using the given
	 * 	samples and the given audio format.
	 *
	 * 	@param samples The samples to buffer.
	 * 	@param af The audio format.
	 */
	public SampleBuffer16Bit( final SampleChunk samples, final AudioFormat af )
	{
		this.format = af;
		this.shortBuffer = samples.getSamplesAsByteBuffer().asShortBuffer();
		this.samples = samples.getSamples();
		this.setStartTimecode( samples.getStartTimecode() );
	}

	/**
	 * 	Create a new 16-bit sample buffer using the given
	 * 	sample format at the given size. It does not scale for
	 * 	the number of channels in the audio format, so you must pre-multiply
	 * 	the number of samples by the number of channels if you are only
	 * 	counting samples per channel.
	 *
	 * 	@param af The audio format of the samples
	 * 	@param nSamples The number of samples
	 */
	public SampleBuffer16Bit( final AudioFormat af, final int nSamples )
	{
		this.format = af.clone();
		this.samples = new byte[ nSamples * 2 ];
		this.shortBuffer = new SampleChunk(this.samples,this.format)
			.getSamplesAsByteBuffer().asShortBuffer();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getSampleChunk()
	 */
	@Override
	public SampleChunk getSampleChunk()
	{
		final SampleChunk sc = new SampleChunk( this.samples, this.format );
		sc.setStartTimecode( this.timecode );
		return sc;
	}

	/**
	 *	{@inheritDoc}
	 *
	 *	Note that because we cannot use native methods for copying parts of
	 *	an array, we must use Java methods so this will be considerably
	 *	slower than {@link #getSampleChunk()}.
	 *
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getSampleChunk(int)
	 */
	@Override
	public SampleChunk getSampleChunk( final int channel )
	{
		if( channel > this.format.getNumChannels() ) {
            throw new IllegalArgumentException( "Cannot generate sample chunk " +
					"for channel "+channel+" as sample only has " +
					this.format.getNumChannels() + " channels." );
        }

		if( channel == 0 && this.format.getNumChannels() == 1 ) {
            return this.getSampleChunk();
        }

		final byte[] newSamples = new byte[this.size()*2];
		final ShortBuffer sb = ByteBuffer.wrap( newSamples ).order(
			this.format.isBigEndian()?ByteOrder.BIG_ENDIAN:ByteOrder.LITTLE_ENDIAN ).
			asShortBuffer();
		for( int i = 0; i < this.size()/this.format.getNumChannels(); i++ ) {
            sb.put( i, this.shortBuffer.get( i*this.format.getNumChannels() + channel ) );
        }

		final AudioFormat af = this.format.clone();
		af.setNumChannels( 1 );
		return new SampleChunk( newSamples, af );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#get(int)
	 */
	@Override
	public float get( final int index )
	{
		if( index >= this.shortBuffer.limit() ) {
            return 0;
        }

		// Convert the short to an integer
		return (float)this.shortBuffer.get(index) * Integer.MAX_VALUE / Short.MAX_VALUE;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getUnscaled(int)
	 */
	@Override
	public float getUnscaled( final int index )
	{
		return this.shortBuffer.get(index);
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#set(int, float)
	 */
	@Override
	public void set( final int index, final float sample )
	{
		// Clipping
		float s = sample;
		if( s > Integer.MAX_VALUE ) {
            s = Integer.MAX_VALUE;
        }
		if( s < Integer.MIN_VALUE ) {
            s = Integer.MIN_VALUE;
        }

		this.shortBuffer.put( index, (short)(sample  * Short.MAX_VALUE / Integer.MAX_VALUE) );
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#size()
	 */
	@Override
	public int size()
	{
		return this.shortBuffer.limit();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#getFormat()
	 */
	@Override
	public AudioFormat getFormat()
	{
		return this.format;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#setFormat(org.openimaj.audio.AudioFormat)
	 */
	@Override
	public void setFormat( final AudioFormat af )
	{
		this.format = af;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#asDoubleArray()
	 */
	@Override
	public double[] asDoubleArray()
	{
		final double[] d = new double[this.size()];
		for( int i = 0; i < this.size(); i++ ) {
            d[i] = this.get(i) / Integer.MAX_VALUE;
        }
		return d;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.samples.SampleBuffer#asDoubleChannelArray()
	 */
	@Override
	public double[][] asDoubleChannelArray()
	{
		final int nc = this.format.getNumChannels();
		final double[][] s = new double[nc][this.size()/nc];
		for( int c = 0; c < nc; c++ ) {
            for( int sa = 0; sa < this.size()/nc; sa++ ) {
                s[c][sa] = this.get( sa*nc + c )  / Integer.MAX_VALUE;
            }
        }
		return s;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<Float> iterator()
	{
		this.iteratorCount = 0;
		return this;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		return this.iteratorCount < this.size();
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.util.Iterator#next()
	 */
	@Override
	public Float next()
	{
		final float f = this.get(this.iteratorCount);
		this.iteratorCount++;
		return f;
	}

	/**
	 *	{@inheritDoc}
	 * 	@see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
		throw new NotImplementedException( "Cannot remove from 16bit sample buffer" );
	}

	@Override
	public AudioTimecode getStartTimecode()
	{
		return this.timecode;
	}

	/**
	 * 	Set the timecode for this buffer.
	 *	@param timecode The timecode
	 */
	public void setStartTimecode( final AudioTimecode timecode )
	{
		this.timecode = timecode;
	}
}