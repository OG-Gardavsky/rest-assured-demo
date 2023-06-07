import io.github.cdimascio.dotenv.Dotenv;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.UUID;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;


public class BranchesAPITest {
    private static final String BASE_URL = "https://api.kb.cz/open/api/touchpoints/v1";
    private String API_KEY;

    @BeforeClass
    public void setUp() throws Exception {
        RestAssured.baseURI = BASE_URL;

        Dotenv dotenv = Dotenv
                .configure()
                .directory("./")
                .load();
        String apiKey = dotenv.get("API_KEY");
        if (apiKey == null) {
            throw new Exception("Unable to get 'API_KEY' from .env file ");
        }

        this.API_KEY = apiKey;
    }


    private void getNearestBranches(double latitude, double longitude, int page, String apiKey, int expectedStatusCode, String validationSchemaFileName) {
        ValidatableResponse response = RestAssured
            .given()
                .header("x-correlation-id", UUID.randomUUID())
                .header("x-api-key", "Bearer " + apiKey)
                .queryParam("latitude", latitude)
                .queryParam("longitude", longitude)
                .queryParam("page", page)
            .when()
                .get("/branches/nearest")
            .then()
                .assertThat()
                .statusCode(expectedStatusCode);

        if (validationSchemaFileName != null) {
            response.body(matchesJsonSchemaInClasspath(validationSchemaFileName));
        }
    }

    private void getNearestBranches(double latitude, double longitude, int page, String apiKey, int expectedStatusCode) {
        getNearestBranches(latitude, longitude, page, apiKey, expectedStatusCode, null);
    }

    @Test
    public void getNearestBranches_Success() {
        getNearestBranches(49, 14, 0, API_KEY, 200, "nearest_branches-schema.json");
    }

    @Test
    public void getNearestBranches_emptyApikey() {
        getNearestBranches(49, 14, 0, "", 401);
    }

    @Test
    public void getNearestBranches_invalidAPiKey() {
        getNearestBranches(49, 14, 0, "invalid-api-key", 401);
    }

    @Test
    public void getNearestBranches_InvalidLatitude() {
        getNearestBranches(49, 181, 0, API_KEY, 400);
    }

    @Test
    public void getNearestBranches_InvalidLongitude() {
        getNearestBranches(91, 14, 0, API_KEY, 400);
    }
}
