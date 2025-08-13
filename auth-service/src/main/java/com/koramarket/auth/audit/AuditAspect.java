package com.koramarket.auth.audit;

import com.koramarket.auth.model.AuditLog;
import com.koramarket.auth.model.User;
import com.koramarket.auth.repository.UserRepository;
import com.koramarket.auth.servce.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @AfterReturning("execution(public * com.koramarket.auth.service.*.*(..)) && !execution(* com.koramarket.auth.service.AuditLogService.save(..))")
    public void auditAllServiceMethods(JoinPoint joinPoint) {
        // Récupération de l'utilisateur courant
        String username = (SecurityContextHolder.getContext().getAuthentication() != null)
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "anonymous";

        User user = userRepository.findByEmailWithRoles(username).orElse(null);

        // Récupération de la requête HTTP pour IP et User-Agent
        HttpServletRequest request = null;
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        }
        String ip = request != null ? request.getRemoteAddr() : "N/A";
        String userAgent = request != null ? request.getHeader("User-Agent") : "N/A";

        // Détails de l'action
        String action = joinPoint.getSignature().getName(); // nom de la méthode appelée
        String endpoint = joinPoint.getSignature().toShortString();

        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEndpoint(endpoint);
        auditLog.setDate(LocalDateTime.now());
        auditLog.setIpAdresse(ip);
        auditLog.setDetails("User-Agent: " + userAgent + "; Args: " + Arrays.toString(joinPoint.getArgs()));

        auditLogService.save(auditLog);

        // Log console pour dev
        System.out.println("Audit log enregistré : " + action + " - " + username + " - " + ip);
    }

    @AfterReturning(
            value = "@annotation(com.koramarket.auth.audit.AuditedAction)",
            returning = "result")
    public void logAudit(JoinPoint joinPoint, Object result) {
        String action = ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(AuditedAction.class).value();
        String endpoint = joinPoint.getSignature().toShortString();
        String username = (SecurityContextHolder.getContext().getAuthentication() != null)
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "anonymous";

        User user = userRepository.findByEmailWithRoles(username).orElse(null);

        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEndpoint(endpoint);
        auditLog.setDate(LocalDateTime.now());
        auditLog.setDetails("Arguments: " + java.util.Arrays.toString(joinPoint.getArgs()));

        auditLogService.save(auditLog);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        auditLog.setIpAdresse(ip);
        auditLog.setDetails("User-Agent: " + userAgent + "; Args: " + Arrays.toString(joinPoint.getArgs()));
    }
}
