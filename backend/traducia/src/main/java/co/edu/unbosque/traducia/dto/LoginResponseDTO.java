package co.edu.unbosque.traducia.dto;

public class LoginResponseDTO {
	private UserResponseDTO userResponse;
	private String jwt;

	public UserResponseDTO getUserResponse() {
		return userResponse;
	}

	public void setUserResponse(UserResponseDTO userResponse) {
		this.userResponse = userResponse;
	}

	public String getJwt() {
		return jwt;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}

	public LoginResponseDTO(UserResponseDTO userResponse, String jwt) {
		this.userResponse = userResponse;
		this.jwt = jwt;
	}

	public LoginResponseDTO() {

	}

}
