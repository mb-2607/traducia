package co.edu.unbosque.traducia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user")
public class User {
	
	private static final long serialVersionUID = 1L;
	
	private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) long id;
	
	@Column(unique = true)
	private String username;
	
	@Column(unique = true)
	private String email;
	
	private String password;
	
	@Enumerated(EnumType.STRING)
	private Role role;
	
	@Enumerated(EnumType.STRING)
	private PlanType planType;
	
	private boolean enabled;
	
	private boolean accountNonLocked = true;
	
	private boolean debeCambiarPassword;
	
	public User() {
		this.accountNonLocked = true;
		this.debeCambiarPassword = false;
		this.enabled = true;
		this.role = Role.USER;
		this.planType = PlanType.FREE;
	}
	
	public User(String username, String password) {
		this();
		this.username = username;
		this.password = password;
	}
	
	public User(String username, String password, Role role) {
		this();
		this.username = username;
		this.password = password;
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public PlanType getPlanType() {
		return planType;
	}

	public void setPlanType(PlanType planType) {
		this.planType = planType;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isAccountNonLocked() {
		return accountNonLocked;
	}

	public void setAccountNonLocked(boolean accountNonLocked) {
		this.accountNonLocked = accountNonLocked;
	}

	public boolean isDebeCambiarPassword() {
		return debeCambiarPassword;
	}

	public void setDebeCambiarPassword(boolean debeCambiarPassword) {
		this.debeCambiarPassword = debeCambiarPassword;
	}

	public long getId() {
		return id;
	}
	
}
