package com.ncgroup.marketplaceserver.repository.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ncgroup.marketplaceserver.model.Role;
import com.ncgroup.marketplaceserver.model.User;
import com.ncgroup.marketplaceserver.model.mapper.UserRowMapper;
import com.ncgroup.marketplaceserver.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@PropertySource("classpath:database/queries.properties")
public class UserRepositoryImpl implements UserRepository {

	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	
	@Value("${user.find-by-email}")
	private String findByEmailQuery;
	
	@Value("${user.find-by-id}")
	private String findByIdQuery;
	
	@Value("${role.find}")
	private String findRoleQuery;
	
	@Value("${user.insert-credentials}")
	private String insertCredentialsQuery;
	
	@Value("${user.insert}")
	private String insertUserQuery;
	
	@Value("${user.update-last-failed-auth}")
	private String updateLastFailedAuthQuery;
	
	@Autowired
	public UserRepositoryImpl(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}
	
	/*Returns User if exists or else null*/
	@Override
	public User findByEmail(String email) {
		Object[] params = {email};
		List<User> users = jdbcTemplate.query(findByEmailQuery, new UserRowMapper(), params);
		log.info("FIND USER BY EMAIL");
		return users.isEmpty() ? null : users.get(0);
	}

	@Override
	@Transactional
	public User save(User user) {
		KeyHolder credentialsHolder = new GeneratedKeyHolder();
		SqlParameterSource credentialsParameters = new MapSqlParameterSource()
				.addValue("role_id", getRoleId(Role.ROLE_USER))
				.addValue("email", user.getEmail())
				.addValue("password", user.getPassword())
				.addValue("is_enabled", user.isEnabled())
				.addValue("failed_auth", credentialsHolder)
				.addValue("last_failed_auth", user.getLastFailedAuth());
		namedParameterJdbcTemplate.update(insertCredentialsQuery, credentialsParameters, credentialsHolder);
		
		KeyHolder userHolder = new GeneratedKeyHolder();
		SqlParameterSource userParameters = new MapSqlParameterSource()
				.addValue("credentials_id", credentialsHolder.getKey().longValue())
				.addValue("name", user.getName())
				.addValue("surname", user.getSurname())
				.addValue("phone", user.getPhone());

		namedParameterJdbcTemplate.update(insertUserQuery, userParameters, userHolder);
		user.setId(userHolder.getKey().longValue());
		return user;
	}

	@Override
	public List<User> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User findById(long id) {
		Object[] params = {id};
		List<User> users = jdbcTemplate.query(findByIdQuery, new UserRowMapper(), params);
		log.info("FIND USER BY ID");
		return users.isEmpty() ? null : users.get(0);
	}

	//@Override
	/*public void updateLastFailedAuth(long id, int lastFailedAuth) {
		Object[] params = {lastFailedAuth, email};
		jdbcTemplate.update(updateLastFailedAuthQuery, params);
	}*/

	@Override
	public void updateLastFailedAuth(String email, int lastFailedAuth) {
		Object[] params = {lastFailedAuth, email};
		jdbcTemplate.update(updateLastFailedAuthQuery, params);
	}

	@Override
	public void deleteById(long id) {
		// TODO Auto-generated method stub
		
	}
	
	private int getRoleId(Role role) { 
		return jdbcTemplate.queryForObject(findRoleQuery, Integer.class, new Object[] {role.name()});
	}

	

}
