package co.edu.unbosque.traducia.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import co.edu.unbosque.traducia.repository.UserRepository;

public class TraduciaUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public TraduciaUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username)
				.orElseThrow(() -> 
				new UsernameNotFoundException("User not found with username: " + username));
	}
}
