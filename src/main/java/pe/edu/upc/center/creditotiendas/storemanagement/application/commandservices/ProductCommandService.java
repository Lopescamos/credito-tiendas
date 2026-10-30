package pe.edu.upc.center.creditotiendas.storemanagement.application.commandservices;

import pe.edu.upc.center.creditotiendas.shared.application.result.ApplicationError;
import pe.edu.upc.center.creditotiendas.shared.application.result.Result;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.aggregates.Product;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.CreateProductCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateProductCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UpdateProductStatusCommand;
import pe.edu.upc.center.creditotiendas.storemanagement.domain.model.commands.UploadProductImageCommand;

public interface ProductCommandService {
    Result<Long, ApplicationError> handle(CreateProductCommand command);
    Result<Product, ApplicationError> handle(UpdateProductCommand command);
    Result<Product, ApplicationError> handle(UpdateProductStatusCommand command);
    Result<Product, ApplicationError> handle(UploadProductImageCommand command);
}
