package br.com.webbudget.infrastructure.spring.security;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetailsService bridging to the existing DB schema.
 *
 * Uses direct JDBC so it works before the configuration entities are ported to
 * the Jakarta namespace. Once the [port] configuration issue is done, replace
 * this with a proper UserRepository injection.
 *
 * Permission model mirrors the Shiro realm: each authority is
 * "functionality:permission" from configuration.authorizations via
 * configuration.grants → configuration.groups → configuration.users.
 */
@Service
public class WebBudgetUserDetailsService implements UserDetailsService {

    private static final String LOAD_USER_SQL = """
        SELECT u.username, u.password, u.active,
               a.functionality, a.permission
          FROM configuration.users u
          LEFT JOIN configuration.groups g ON g.id = u.id_group
          LEFT JOIN configuration.grants gr ON gr.id_group = g.id
          LEFT JOIN configuration.authorizations a ON a.id = gr.id_authorization
         WHERE u.username = ?
        """;

    private final JdbcTemplate jdbc;

    public WebBudgetUserDetailsService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<Map<String, Object>> rows = jdbc.queryForList(LOAD_USER_SQL, username);
        if (rows.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        Map<String, Object> first = rows.get(0);
        boolean active = Boolean.TRUE.equals(first.get("active"));
        String password = (String) first.get("password");

        Set<GrantedAuthority> authorities = rows.stream()
            .filter(r -> r.get("functionality") != null && r.get("permission") != null)
            .map(r -> new SimpleGrantedAuthority(r.get("functionality") + ":" + r.get("permission")))
            .collect(Collectors.toSet());

        return new User(username, password, active, true, true, true, authorities);
    }
}
