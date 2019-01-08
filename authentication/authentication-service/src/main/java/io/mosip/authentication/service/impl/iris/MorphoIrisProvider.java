package io.mosip.authentication.service.impl.iris;

import org.springframework.stereotype.Component;

import io.mosip.authentication.core.spi.irisauth.provider.IrisProvider;


/**
 * @author Arun Bose S
 * 
 * The Class MorphoIrisProvider.
 */
@Component
public class MorphoIrisProvider extends IrisProvider {

	/* (non-Javadoc)
	 * @see io.mosip.authentication.core.spi.bioauth.provider.MosipBiometricProvider#createMinutiae(byte[])
	 */
	@Override
	public String createMinutiae(byte[] inputImage) {
		// TODO Auto-generated method stub
		return null;
	}

}
