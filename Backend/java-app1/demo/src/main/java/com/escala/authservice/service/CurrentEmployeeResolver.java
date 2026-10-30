package com.escala.authservice.service;

import com.escala.authservice.entity.Employee;
import com.escala.authservice.entity.User;
import com.escala.authservice.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolve o funcionario ligado ao usuario autenticado, dentro da empresa dele. */
@Component
@RequiredArgsConstructor
public class CurrentEmployeeResolver {
    private final EmployeeRepository employeeRepository;

    public Employee resolve(User user) {
        String companySlug = user.getCompany() == null ? null : user.getCompany().getSlug();
        return employeeRepository.findByUserEmailAndCompanySlug(user.getEmail(), companySlug)
                .or(() -> employeeRepository.findByEmailAndCompanySlug(user.getEmail(), companySlug))
                .orElseThrow();
    }
}
