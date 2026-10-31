package pe.edu.upc.center.creditotiendas.creditmanagement.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * The part of a payment applied to one component of one installment (model: Imputacion_Pago keeps one row per
 * installment with its late fee, interest and capital amounts).
 */
public record PaymentAllocation(int installmentNumber, PaymentComponents component, BigDecimal amount) {
}
