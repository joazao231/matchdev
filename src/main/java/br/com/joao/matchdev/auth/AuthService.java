package br.com.joao.matchdev.auth;

import java.util.Locale;

import br.com.joao.matchdev.candidate.CandidateProfile;
import br.com.joao.matchdev.candidate.CandidateProfileRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final CandidateProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserAccountRepository userRepository,
            CandidateProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Já existe uma conta com este e-mail");
        }

        UserAccount user = userRepository.save(new UserAccount(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password())));
        profileRepository.save(new CandidateProfile(user));
        return responseFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        UserAccount user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("E-mail ou senha inválidos"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("E-mail ou senha inválidos");
        }
        return responseFor(user);
    }

    private AuthResponse responseFor(UserAccount user) {
        return new AuthResponse(
                jwtService.generate(user),
                "Bearer",
                jwtService.expirationSeconds(),
                user.getFullName(),
                user.getEmail());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
