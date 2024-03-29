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
package org.knime.base.node.audio.ext.org.openimaj.audio.filters;

import org.knime.base.node.audio.ext.org.openimaj.audio.AudioStream;
import org.knime.base.node.audio.ext.org.openimaj.audio.SampleChunk;
import org.knime.base.node.audio.ext.org.openimaj.audio.processor.FixedSizeSampleAudioProcessor;
import org.knime.base.node.audio.ext.org.openimaj.audio.samples.SampleBuffer;

/**
 *	FIR filter is a multiplication of the frequency domain of a signal with the frequency
 *	domain of a filter. That corresponds to a convolution in the time domain; that is,
 *	a weighted sum of the previous input.
 *
 *	@author David Dupplaw (dpd@ecs.soton.ac.uk)
 *  @created 22 May 2013
 */
public abstract class FIRFilter extends FixedSizeSampleAudioProcessor
{
	/** The coefficients for this filter */
	private final double[] coefficients;

	/**
	 *	@param stream
	 */
	public FIRFilter( final AudioStream stream )
	{
		super( stream, 4, 3 );

		this.coefficients = this.getCoefficients();
		super.setWindowSize( this.coefficients.length );
		super.setWindowStep( this.coefficients.length-1 );
	}

	/**
	 * 	Returns the coefficients for the particular filter
	 *	@return coefficients
	 */
	public abstract double[] getCoefficients();

	/**
	 *	{@inheritDoc}
	 * 	@see org.openimaj.audio.processor.FixedSizeSampleAudioProcessor#process(org.openimaj.audio.SampleChunk)
	 */
	@Override
	public SampleChunk process( final SampleChunk sample ) throws Exception
	{
		final SampleBuffer sb = sample.getSampleBuffer();

		for( int c = 0; c < sample.getFormat().getNumChannels(); c++ )
		{
			float acc = 0;
			for( int i = 0; i < this.coefficients.length; i++ ) {
                acc += sb.get(i) * this.coefficients[i];
            }
			sb.set( 0, acc );
		}

		return sample;
	}
}
