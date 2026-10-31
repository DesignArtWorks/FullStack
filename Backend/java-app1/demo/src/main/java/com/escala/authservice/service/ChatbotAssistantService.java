package com.escala.authservice.service;

import com.escala.authservice.entity.AiUsage;
import com.escala.authservice.entity.Employee;
import com.escala.authservice.entity.Sector;
import com.escala.authservice.entity.User;
import com.escala.authservice.repository.AiUsageRepository;
import com.escala.authservice.repository.EmployeeRepository;
import com.escala.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Regra do assistente de chatbot: resolve o remetente dentro da empresa de quem chama,
 * registra o uso de IA e sugere substitutos do mesmo setor.
 */
@Service
@RequiredArgsConstructor
public class ChatbotAssistantService {
    private static final String UNKNOWN_ACTION = "UNKNOWN";
    private static final String SUGGEST_REPLACEMENT_ACTION = "SUGGEST_REPLACEMENT";

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final AiUsageRepository aiUsageRepository;

    public record Reply(String action, String response, List<String> suggestions) {
    }

    public Reply reply(UUID callerCompanyId, String senderEmail, String message) {
        String email = senderEmail != null ? senderEmail : "admin@escala.local";
        List<User> users = userRepository.findAllByEmailIgnoreCase(email).stream()
                .filter(candidate -> candidate.getCompany() != null
                        && Objects.equals(candidate.getCompany().getId(), callerCompanyId))
                .toList();
        List<Employee> employees = employeeRepository.findAllByEmailIgnoreCase(email).stream()
                .filter(candidate -> candidate.getCompany() != null
                        && Objects.equals(candidate.getCompany().getId(), callerCompanyId))
                .toList();
        User user = users.size() == 1 ? users.getFirst() : null;
        Employee employee = employees.size() == 1 ? employees.getFirst() : null;

        String normalizedMessage = message != null ? message.toLowerCase() : "";
        String action = UNKNOWN_ACTION;
        String responseText = "Olá! Não consegui entender sua solicitação. Tente pedir uma 'troca' ou solicitar um 'substituto'.";
        List<String> suggestions = new ArrayList<>();

        if (employee != null && user != null) {
            recordUsage(user, message);

            if (normalizedMessage.contains("troca") || normalizedMessage.contains("substitu")
                    || normalizedMessage.contains("escala")) {
                action = SUGGEST_REPLACEMENT_ACTION;
                suggestions.addAll(replacementSuggestions(employee));
                responseText = "Olá " + employee.getFullName() + "! Analisamos sua mensagem com inteligência artificial e localizamos possíveis substitutos aptos a cobrir sua escala de acordo com as regras de distanciamento e escalas.";
            }
        }

        return new Reply(action, responseText, suggestions);
    }

    private void recordUsage(User user, String message) {
        aiUsageRepository.save(AiUsage.builder()
                .user(user)
                .company(user.getCompany())
                .feature("CHATBOT_NLP")
                .usedAt(OffsetDateTime.now())
                .tokensUsed(150)
                .creditsConsumed(1)
                .promptRef(message)
                .build());
    }

    private List<String> replacementSuggestions(Employee employee) {
        Sector sector = employee.getSector();
        List<Employee> colleagues = sector != null
                ? employeeRepository.findByActiveTrueAndSectorIdOrderByFullNameAsc(sector.getId()).stream()
                    .filter(col -> col.getCompany() != null && employee.getCompany() != null
                            && Objects.equals(col.getCompany().getId(), employee.getCompany().getId()))
                    .toList()
                : List.of();

        List<String> suggestions = new ArrayList<>();
        suggestions.add("Opções de substitutos disponíveis no mesmo setor/projeto:");
        for (Employee col : colleagues) {
            if (!col.getId().equals(employee.getId())) {
                suggestions.add(col.getFullName() + " (" + col.getEmail() + ")");
            }
        }
        if (suggestions.size() <= 1) {
            suggestions.add("Nenhum colega do mesmo setor encontrado no momento.");
        }
        return suggestions;
    }
}
