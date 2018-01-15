package hello;


import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

//  Test for all the CRUD options on The Spring project

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HelloControllerIT {

    //  Port
    private int port = 8080;

    final private String name1 = "Alberto";
    final private String address1 = "Arlozorov 5, Tel-Aviv";
    final private String phone1 = "054-123-456";
    private String id1;

    final private String name2 = "Robert";
    final private String address2 = "5th Avenue, New York";
    final private String phone2 = "054-654-321";
    private String id2;

    URL newURL;

    ResponseEntity<String> response;

    //  URL components
    private URL base;
    private String prefix = "http://localhost:" + port + "/customer-api/";

    final private String deleteAllURL = "delete-all";
    final private String createURL = "create?";
    final private String getURL = "get/";
    final private String updateURL = "update/";
    final private String deleteURL = "delete/";

    final private String nameURL = "name";
    final private String addressURL = "address";
    final private String phoneURL = "phone";
    final private String idURL = "id";

    final private String readAllURL = "/read-all";

    final private String andURL = "&";
    final private String equalURL = "=";
    final private String questionURL = "?";

    @Autowired
    private TestRestTemplate template;


    //  Before starting the tests usings API's "delete-all" command to clear the database
    //  and creating two instances for the database
    @Before
    public void setUp() throws Exception {

        this.base = new URL(prefix);

        //  Clears database
        this.response = template.getForEntity(this.base + deleteAllURL.toString(), String.class);

        //  Adds two users to the database
        this.newURL = generateCreateUserURL(name1, address1, phone1);
        response = template.getForEntity(newURL.toString(), String.class);
        id1 = getIdFromJSON(new JSONObject(response.getBody()));

        newURL = generateCreateUserURL(name2, address2, phone2);
        response = template.getForEntity(newURL.toString(), String.class);
        id2 = getIdFromJSON(new JSONObject(response.getBody()));

    }



    //  Tests if can get all instances from data base, should be 2 that added in the "Before"
    @Test
    public void getAll() throws Exception {
        this.newURL = new URL(base + readAllURL);

        this.response = template.getForEntity(newURL.toString(), String.class);

        //  Checks if there are exactly 2 instances in database
        boolean hasAtLeastTwoInstances = false;
        try {
            JSONArray jsonArray = new JSONArray(response.getBody());

            if (jsonArray.length() == 2) {
                hasAtLeastTwoInstances = true;
            }
        } catch (com.oracle.javafx.jmx.json.JSONException e) {
            e.printStackTrace();
        }

        assertTrue(200 == response.getStatusCodeValue());
        assertTrue(hasAtLeastTwoInstances);
    }



    //  Getting user by id that should exist in database and all the fields are as defined
    @Test
    public void getUser() throws Exception {

        Boolean goodUser = false;
        Boolean badUser = false;

        this.newURL = generateGetUserURL(id1);

        this.response = template.getForEntity(newURL.toString(), String.class);

        //  Checks if the fields were updated as they should have been
        try {
            JSONObject jsonObject = new JSONObject(response.getBody());
            if (checkFields(jsonObject)) {
                if (parseAndCheckIfMatches(jsonObject, name1, address1, phone1)) {
                    goodUser = true;
                }
                if (parseAndCheckIfMatches(jsonObject, name2, address1, phone1)) {
                    badUser = true;
                }
            }
        } catch (com.oracle.javafx.jmx.json.JSONException e) {
            e.printStackTrace();
        }

        assertTrue(200 == response.getStatusCodeValue());
        assertTrue(goodUser);
        assertFalse(badUser);
    }



    //  Adding new costumer
    @Test
    public void addUser() throws Exception {

        this.newURL = generateCreateUserURL(name1, address1, phone1);

        this.response = template.getForEntity(newURL.toString(), String.class);

        assertTrue(200 == response.getStatusCodeValue());

    }



    //  Adding user with missing parameter
    @Test
    public void addBadUser() throws Exception {
        this.newURL = new URL(base + createURL + nameURL + equalURL + name1 + andURL + addressURL + equalURL +
                address1);

        this.response = template.getForEntity(newURL.toString(), String.class);

        assertTrue(400 == response.getStatusCodeValue());
    }



    //  Testing update all fields of user and one field
    @Test
    public void updateUser() throws Exception {

        //  Updating the the details of first costumer to those of the second
        this.newURL = new URL(base + updateURL + id1 + questionURL + nameURL + equalURL + name2 + andURL +
        addressURL + equalURL + address2 + andURL + phoneURL + equalURL + phone2);

        this.response = template.getForEntity(newURL.toString(), String.class);

        //  Changing one of the fields back
        this.newURL = new URL(base + updateURL + id1 + questionURL + nameURL + equalURL + name1);
        this.response = template.getForEntity(newURL.toString(), String.class);

        //  Getting the first customer after changes
        this.newURL = generateGetUserURL(id1);

        response = template.getForEntity(newURL.toString(), String.class);

        JSONObject jsonObject = new JSONObject(response.getBody());

        assertTrue(200 == response.getStatusCodeValue());

        //  Chceking if the fields changed as expected
        assertTrue(parseAndCheckIfMatches(jsonObject, name1, address2, phone2));
    }



    //  Tests deleting costumer
    @Test
    public void deleteUser() throws Exception {

        //  Deletes user
        this.newURL = generateDeleteUserURL(id1);
        this.response = template.getForEntity(newURL.toString(), String.class);

        // Trying to get the user with the deleted Id
        this.newURL = generateGetUserURL(id1);
        this.response = template.getForEntity(newURL.toString(), String.class);

        //  Checking if exception was trown
        assertTrue(500 == response.getStatusCodeValue());
    }



    //  Test deleting all costumers
    @Test
    public void deleteAll() throws MalformedURLException {

        //  Deleting all data
        this.newURL = new URL(this.base + deleteAllURL.toString());
        this.response = template.getForEntity(newURL.toString(), String.class);

        assertTrue(200 == this.response.getStatusCodeValue());

        //  Checking if empty now
        this.newURL = new URL(base + readAllURL);
        this.response = template.getForEntity(newURL.toString(), String.class);

        boolean hasNoInstances = false;
        try {
            JSONArray jsonArray = new JSONArray(response.getBody());

            if (jsonArray.length() == 0) {
                hasNoInstances = true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertTrue(200 == this.response.getStatusCodeValue());
        assertTrue(hasNoInstances);
    }



    @After
    public void setDown() throws Exception {
        //  Clears database
        this.response = template.getForEntity(this.base + deleteAllURL.toString(), String.class);
    }



    //  Methods

    //  Gnerates url for create request
    private URL generateCreateUserURL(String name, String address, String phone) throws MalformedURLException {

        URL newURL = new URL(base + createURL + nameURL + equalURL + name + andURL + addressURL + equalURL +
                address + andURL + phoneURL + equalURL + phone);
        return newURL;

    }

    //  Generates URL for get user request
    private URL generateGetUserURL(String id) throws MalformedURLException {
        URL newURL = new URL(base + getURL + id1);
        return newURL;
    }

    private URL generateDeleteUserURL (String id) throws MalformedURLException {
        URL newURL = new URL(base + deleteURL + id1);
        return newURL;
    }

    //  Checks if all fields exist in JSONObject that represents custumer
    private boolean checkFields(JSONObject jsonObject) {
        if (jsonObject.has(nameURL) && jsonObject.has(addressURL) && jsonObject.has(phoneURL) && jsonObject.has(idURL)) {
            return true;
        }
        return false;
    }

    //  Parsing json and checking if the the values are equal to recieved values
    private boolean parseAndCheckIfMatches(JSONObject jsonObject, String name, String address, String phone) throws JSONException {
        if ((name.equals(jsonObject.getString(nameURL))) && (address.equals(jsonObject.getString(addressURL)))
                && (phone.equals(jsonObject.getString(phoneURL)))) {
            return true;
        }
        return false;
    }

    //  Parsing json to get users id
    private String getIdFromJSON(JSONObject jsonObject) throws JSONException {
        return jsonObject.getString(idURL);
    }


}