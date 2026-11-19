package co.edu.unbosque.traducia.exception;

public class PlanCharacterLimitExceededException extends RuntimeException {

	public PlanCharacterLimitExceededException() {
	}

	public PlanCharacterLimitExceededException(String message) {
		super(message);
	}

}
