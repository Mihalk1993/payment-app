package eu.paze.paymentapp.controller;

import eu.paze.paymentapp.model.PaymentRequest;
import eu.paze.paymentapp.service.PaymentService;
import eu.paze.paymentapp.util.exception.PaymentException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/payment")
    public String showPaymentForm(Model model) {
        model.addAttribute("paymentRequest", new PaymentRequest());
        return "payment";
    }

    @PostMapping("/payment")
    public String processPayment(@ModelAttribute("paymentRequest") PaymentRequest paymentRequest, RedirectAttributes redirectAttributes) {
        try {
            String redirectUrl = paymentService.createPayment(paymentRequest.getAmount());
            return "redirect:" + redirectUrl;
        } catch (PaymentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/error";
        }
    }

    @GetMapping("/error")
    public String showErrorPage(Model model, @ModelAttribute("errorMessage") String errorMessage) {
        model.addAttribute("errorMessage", errorMessage);
        return "error";
    }
}