package br.com.joao.matchdev.candidate;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import br.com.joao.matchdev.auth.UserAccount;
import org.junit.jupiter.api.Test;

class CandidateProfileTests {

    @Test
    void shouldImportOnlyNewResumeSkillsAndSuggestRoleForAnEmptyProfile() {
        UserAccount user = new UserAccount("João", "joao@example.com", "hash");
        CandidateProfile profile = new CandidateProfile(user);

        int firstImport = profile.mergeResumeSkills(
                Set.of("java", "spring boot", "postgresql"),
                "Desenvolvedor Backend");
        int repeatedImport = profile.mergeResumeSkills(
                Set.of("java", "spring boot"),
                "Outro cargo");

        assertThat(firstImport).isEqualTo(3);
        assertThat(repeatedImport).isZero();
        assertThat(profile.getDesiredRole()).isEqualTo("Desenvolvedor Backend");
        assertThat(profile.getSkills()).containsExactlyInAnyOrder("java", "spring boot", "postgresql");
    }
}
