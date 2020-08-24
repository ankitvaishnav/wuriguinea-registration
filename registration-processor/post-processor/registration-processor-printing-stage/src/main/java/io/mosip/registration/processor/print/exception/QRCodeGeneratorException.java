package io.mosip.registration.processor.print.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;

public class QRCodeGeneratorException extends BaseUncheckedException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public QRCodeGeneratorException() {
		super();
	}
	
	/**
	 * Instantiates a new unexpected exception.
	 *
	 * @param message the message
	 */
	public QRCodeGeneratorException(String message) {
		super(PlatformErrorMessages.RPR_PRT_QR_CODE_GENERATION_ERROR.getCode(), message);
	}

	/**
	 * Instantiates a new unexpected exception.
	 *
	 * @param message the message
	 * @param cause the cause
	 */
	public QRCodeGeneratorException(String message, Throwable cause) {
		super(PlatformErrorMessages.RPR_PRT_QR_CODE_GENERATION_ERROR.getCode() + EMPTY_SPACE, message, cause);
	}
	
}
