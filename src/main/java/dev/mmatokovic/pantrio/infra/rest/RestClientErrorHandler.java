package dev.mmatokovic.pantrio.infra.rest;

import dev.mmatokovic.pantrio.app.product.ProductException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

class RestClientErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().isError();
    }

    @Override
    public void handleError(@NonNull URI url, @NonNull HttpMethod method, @NonNull ClientHttpResponse response) throws IOException {
        MediaType contentType = response.getHeaders().getContentType();

        if (contentType == null || !contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            throw new ProductException(
                "OpenFoodFacts API error: status=%s, content-type=%s".formatted(
                    response.getStatusCode(), contentType
                )
            );
        }

        ResponseErrorHandler.super.handleError(url, method, response);
    }
}
