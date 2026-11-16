package co.edu.unbosque.traducia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.unbosque.springfirstapp.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
	public Optional<User> findByUsername(String username);
	public void deleteByUsername(String username); 

}
