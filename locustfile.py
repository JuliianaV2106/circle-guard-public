from locust import HttpUser, task, between

class GatewayUser(HttpUser):
    wait_time = between(1, 3)
    host = "http://localhost:31449"

    @task(3)
    def validate_valid_token(self):
        """Simula validación de token válido"""
        self.client.post(
            "/api/v1/gate/validate",
            json={"token": "valid-test-token"},
            name="POST /gate/validate [valid]"
        )

    @task(2)
    def validate_invalid_token(self):
        """Simula validación de token inválido"""
        self.client.post(
            "/api/v1/gate/validate",
            json={"token": "invalid-token-xyz"},
            name="POST /gate/validate [invalid]"
        )

    @task(1)
    def validate_empty_token(self):
        """Simula request sin token"""
        self.client.post(
            "/api/v1/gate/validate",
            json={},
            name="POST /gate/validate [empty]"
        )