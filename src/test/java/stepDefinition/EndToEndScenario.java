package stepDefinition;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.List;
import java.util.Map;

public class EndToEndScenario {

	private static final String USER_ID = "55708b1a-69d6-4792-85c9-3c580bd36bdc";
	private static final String BASE_URL = "https://demoqa.com";
	private static final String USERNAME = "API_Class";
	private static final String PASSWORD = "Apiclass@2021";
	private static String token;
	private static String deleteBody;
	private static String jsonString;
	private static String bookID;

	@Given("I am Authorized User")
	public void verifyAuthorisedUser() {
		RestAssured.baseURI = BASE_URL;
		RequestSpecification request = RestAssured.given();
		
		String requestBody = "{\r\n" + "  \"userName\": \""+USERNAME+"\",\r\n" + "  \"password\": \""+PASSWORD+"\"\r\n"
				+ "}";
		request.header("Content-Type", "application/json");
		Response tokenResponse = request.body(requestBody).request(Method.POST, "/Account/v1/GenerateToken");

		Assert.assertEquals(tokenResponse.getStatusCode(),200);

		token = tokenResponse.getBody().path("token");
	}

	@When("A list of books is available")
	public void checkListOfBooksAvailable() {
		RestAssured.baseURI = BASE_URL;
		RequestSpecification request = RestAssured.given();

		request.header("Content-Type", "application/json");

		Response booksResponse = request.request(Method.GET, "/BookStore/v1/Books");
		Assert.assertEquals(booksResponse.getStatusCode(),200);

		jsonString = booksResponse.asString();

		List<Map<String,String>> books = JsonPath.from(jsonString).get("books");
		Assert.assertTrue(books.size() > 0);

		bookID = books.get(1).get("isbn");

	}

	@When("I assign a book to myself")
	public void assignBook() {
		RestAssured.baseURI = BASE_URL;
		RequestSpecification request = RestAssured.given();

		request.header("Authorization", "Bearer " + token)
				.header("Content-Type", "application/json");

		String addBookDetails = "{\r\n" + "  \"userId\": \""+USER_ID+"\",\r\n"
				+ "  \"collectionOfIsbns\": [\r\n" + "    {\r\n" + "      \"isbn\": \""+bookID+"\"\r\n" + "    }\r\n"
				+ "  ]\r\n" + "}";

		Response addBooksResponse = request.body(addBookDetails).post("/BookStore/v1/Books");

		Assert.assertEquals(addBooksResponse.getStatusCode(), 201);
	}

	@Then("I remove the book")
	public void removeBook() {
		RestAssured.baseURI = BASE_URL;
		RequestSpecification request = RestAssured.given();

		deleteBody = "{\r\n" + "  \"isbn\": \""+bookID+"\",\r\n"
				+ "  \"userId\": \""+USER_ID+"\"\r\n" + "}";

		request.header("Authorization", "Bearer " + token).header("Content-Type", "application/json");
		Response responseDelete = request.body(deleteBody).delete("/BookStore/v1/Book");

		Assert.assertEquals(responseDelete.getStatusCode(),204);
	}

	@Then("I confirm the book is removed")
	public void confirmBookRemoved() {
		RestAssured.baseURI = BASE_URL;
		RequestSpecification request = RestAssured.given();

		request.header("Authorization", "Bearer " + token).header("Content-Type", "application/json");

		Response responseDeleteConfirm = request.body(deleteBody).delete("/BookStore/v1/Book");

		Assert.assertEquals(responseDeleteConfirm.getStatusCode(), 400);

		Assert.assertEquals(responseDeleteConfirm.getBody().path("message"),
				"ISBN supplied is not available in User's Collection!");
	}

}
