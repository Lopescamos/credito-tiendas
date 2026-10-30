package pe.edu.upc.center.creditotiendas.shared.application.result;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    void mapAndFlatMapTransformOnlySuccess() {
        Result<Integer, ApplicationError> success = Result.success(2);

        var result = success.map(v -> v * 10).flatMap(v -> Result.<String, ApplicationError>success("v=" + v));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrElse("none")).isEqualTo("v=20");
    }

    @Test
    void failureShortCircuitsMapAndFlatMap() {
        var error = ApplicationError.notFound("CreditSale", "7");
        Result<Integer, ApplicationError> failure = Result.failure(error);

        var result = failure.map(v -> v * 10).flatMap(v -> Result.<String, ApplicationError>success("never"));

        assertThat(result.isFailure()).isTrue();
        assertThat(result.toOptional()).isEmpty();
        assertThat(((Result.Failure<String, ApplicationError>) result).error()).isEqualTo(error);
    }

    @Test
    void recoverReplacesFailureButKeepsSuccess() {
        Result<Integer, String> failure = Result.failure("boom");
        Result<Integer, String> success = Result.success(1);

        assertThat(failure.recover(e -> Result.success(0)).getOrElse(-1)).isZero();
        assertThat(success.recover(e -> Result.success(0)).getOrElse(-1)).isEqualTo(1);
    }

    @Test
    void mapErrorTransformsOnlyFailure() {
        Result<Integer, String> failure = Result.failure("boom");

        var mapped = failure.mapError(String::length);

        assertThat(((Result.Failure<Integer, Integer>) mapped).error()).isEqualTo(4);
    }
}
