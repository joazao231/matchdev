package br.com.joao.matchdev.candidate;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import br.com.joao.matchdev.common.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateProfileService {

    private final CandidateProfileRepository profileRepository;

    public CandidateProfileService(CandidateProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getByUserEmail(String email) {
        return ProfileResponse.from(requireProfile(email));
    }

    @Transactional
    public ProfileResponse update(String email, ProfileUpdateRequest request) {
        CandidateProfile profile = requireProfile(email);
        profile.update(
                request.headline(),
                request.desiredRole(),
                request.location(),
                request.desiredSeniority(),
                normalizeSkills(request.skills()),
                request.preferredWorkModels());
        return ProfileResponse.from(profileRepository.save(profile));
    }

    @Transactional
    public ResumeProfileUpdate mergeResumeSkills(
            String email,
            Set<String> detectedSkills,
            String suggestedRole) {
        CandidateProfile profile = requireProfile(email);
        int importedSkills = profile.mergeResumeSkills(normalizeSkills(detectedSkills), suggestedRole);
        return new ResumeProfileUpdate(ProfileResponse.from(profileRepository.save(profile)), importedSkills);
    }

    public CandidateProfile requireProfile(String email) {
        return profileRepository.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil profissional não encontrado"));
    }

    private Set<String> normalizeSkills(Set<String> skills) {
        Set<String> normalized = new LinkedHashSet<>();
        skills.forEach(skill -> normalized.add(normalize(skill)));
        return normalized;
    }

    private String normalize(String value) {
        String withoutAccents = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT);
    }

    public record ResumeProfileUpdate(ProfileResponse profile, int importedSkills) {
    }
}
