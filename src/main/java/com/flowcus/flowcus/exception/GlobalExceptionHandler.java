package com.flowcus.flowcus.exception;

import com.flowcus.flowcus.dto.UserDisplayDto;
import com.flowcus.flowcus.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.logging.Level;
import java.util.logging.Logger;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final UserService userService;


    // Professional logging so can still see the exact error in console
    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /**
     * Catches any generic Exception
     */
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, Model model) {

        // Log the stack trace to console
//        logger.log(Level.SEVERE, "An unexpected error occurred: ", e);

        // DO NOT pass 'e.getMessage()' because it might contain SQL logic or sensitive data
        model.addAttribute("errorMessage", "Oops! Something went wrong on our end. Please try again later.");
        return "error";
    }
}
