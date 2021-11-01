package faka.cs.tsensor.integration.web;


import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class SensorServiceControllerTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.4.2");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
        registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    }

    @Autowired
    private MockMvc mvc;

    @Test
    public void manySensorTemperatureDataIsAccepted() throws Exception {
        String sensorId = UUID.randomUUID().toString();

        performPut(sensorId, "{\"data\": [" +
                "{ \"pointInTime\" :\"2021-10-31T21:00:00\", \"temperature\": 5.0}," +
                "{ \"pointInTime\" :\"2021-10-31T21:01:00\", \"temperature\": 5.5}," +
                "{ \"pointInTime\" :\"2021-10-31T21:03:00\", \"temperature\": 6}" +
                "]}")
                .andExpect(status().isCreated());
    }

    @Test
    public void anySensorTemperatureDataIsBadRequestWhenPointInTimeIsNull() throws Exception {
        String sensorId = UUID.randomUUID().toString();

        mvc.perform(put("/sensor/" + sensorId + "/temperature", "{\"data\": [" +
                        "{ \"pointInTime\" :\"2021-10-31T21:00:00\", \"temperature\": 5.0}," +
                        "{ \"pointInTime\" :\"2021-10-31T21:01:00\", \"temperature\": 5.5}," +
                        "{ \"pointInTime\" :\"2021-10-31T21:03:00\", \"temperature\": 6}" +
                        "]}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"data\": [" +
                                "{ \"temperature\": 5.0}," +
                                "{ \"pointInTime\" :\"2021-10-31T21:01:00\", \"temperature\": 5.5}," +
                                "{ \"pointInTime\" :\"2021-10-31T21:03:00\", \"temperature\": 6}" +
                                "]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("data[0].pointInTime must not be null"));
    }

    @Test
    public void anySensorTemperatureDataIsBadRequestWhenTemperatureIsNull() throws Exception {
        String sensorId = UUID.randomUUID().toString();

        mvc.perform(put("/sensor/" + sensorId + "/temperature", "{\"data\": [" +
                        "{ \"pointInTime\" :\"2021-10-31T21:00:00\", \"temperature\": 5.0}," +
                        "{ \"pointInTime\" :\"2021-10-31T21:01:00\", \"temperature\": 5.5}," +
                        "{ \"pointInTime\" :\"2021-10-31T21:03:00\", \"temperature\": 6}" +
                        "]}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"data\": [" +
                                "{ \"pointInTime\" :\"2021-10-31T21:01:00\"}," +
                                "{ \"pointInTime\" :\"2021-10-31T21:03:00\", \"temperature\": 6}" +
                                "]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("data[0].temperature must not be null"));
    }

    @Test
    public void sensorTemperatureDataIsBadRequestWhenDataIsNull() throws Exception {
        String sensorId = UUID.randomUUID().toString();

        mvc.perform(put("/sensor/" + sensorId + "/temperature", "{\"data\": [" +
                        "{ \"pointInTime\" :\"2021-10-31T21:00:00\", \"temperature\": 5.0}," +
                        "{ \"pointInTime\" :\"2021-10-31T21:01:00\", \"temperature\": 5.5}," +
                        "{ \"pointInTime\" :\"2021-10-31T21:03:00\", \"temperature\": 6}" +
                        "]}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"data\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage").value("data must not be null"));
    }

    @Test
    public void aggregatedDailyTemperatureIsGetSuccessfully() throws Exception {
        String sensorId = UUID.randomUUID().toString();

        performPut(sensorId, "{\"data\": [" +
                "{ \"pointInTime\" :\"2021-10-29T00:01:00\", \"temperature\": 7.01}," +
                "{ \"pointInTime\" :\"2021-10-29T00:02:00\", \"temperature\": 7.02}," +
                "{ \"pointInTime\" :\"2021-10-29T00:03:00\", \"temperature\": 7.03}," +
                "{ \"pointInTime\" :\"2021-10-29T00:04:00\", \"temperature\": 7.04}," +
                "{ \"pointInTime\" :\"2021-10-29T00:05:00\", \"temperature\": 7.05}," +
                "{ \"pointInTime\" :\"2021-10-30T00:01:00\", \"temperature\": 6.01}," +
                "{ \"pointInTime\" :\"2021-10-30T00:02:00\", \"temperature\": 6.02}," +
                "{ \"pointInTime\" :\"2021-10-30T00:03:00\", \"temperature\": 6.03}," +
                "{ \"pointInTime\" :\"2021-10-30T00:04:00\", \"temperature\": 6.04}," +
                "{ \"pointInTime\" :\"2021-10-30T00:05:00\", \"temperature\": 6.05}," +
                "{ \"pointInTime\" :\"2021-10-31T00:01:00\", \"temperature\": 5.01}," +
                "{ \"pointInTime\" :\"2021-10-31T00:02:00\", \"temperature\": 5.02}," +
                "{ \"pointInTime\" :\"2021-10-31T00:03:00\", \"temperature\": 5.03}," +
                "{ \"pointInTime\" :\"2021-10-31T00:04:00\", \"temperature\": 5.04}," +
                "{ \"pointInTime\" :\"2021-10-31T00:05:00\", \"temperature\": 5.05}" +
                "]}")
                .andExpect(status().isCreated());

        mvc.perform(get("/sensor/" + sensorId + "/temperature/daily")
                        .param("from", "2021-10-30")
                        .param("to", "2021-10-31")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{" +
                                "  \"sensorId\": \"" + sensorId + "\"," +
                                "  \"from\": \"2021-10-30T00:00:00\"," +
                                "  \"to\": \"2021-10-31T00:00:00\"," +
                                "  \"data\": [" +
                                "    {" +
                                "      \"average\": 6.03," +
                                "      \"date\": \"2021-10-30\"" +
                                "    }," +
                                "    {" +
                                "      \"average\": 5.03," +
                                "      \"date\": \"2021-10-31\"" +
                                "    }" +
                                "  ]" +
                                "}"));
    }

    @Test
    public void aggregatedHourlyTemperatureIsGetSuccessfully() throws Exception {
        String sensorId = UUID.randomUUID().toString();

        performPut(sensorId, "{\"data\": [" +
                "{ \"pointInTime\" :\"2021-10-29T00:01:00\", \"temperature\": 7.01}," +
                "{ \"pointInTime\" :\"2021-10-29T00:02:00\", \"temperature\": 7.02}," +
                "{ \"pointInTime\" :\"2021-10-29T00:03:00\", \"temperature\": 7.03}," +
                "{ \"pointInTime\" :\"2021-10-29T00:04:00\", \"temperature\": 7.04}," +
                "{ \"pointInTime\" :\"2021-10-29T00:05:00\", \"temperature\": 7.05}," +
                "{ \"pointInTime\" :\"2021-10-29T01:01:00\", \"temperature\": 7.11}," +
                "{ \"pointInTime\" :\"2021-10-29T01:02:00\", \"temperature\": 7.12}," +
                "{ \"pointInTime\" :\"2021-10-29T01:03:00\", \"temperature\": 7.13}," +
                "{ \"pointInTime\" :\"2021-10-29T01:04:00\", \"temperature\": 7.14}," +
                "{ \"pointInTime\" :\"2021-10-29T01:05:00\", \"temperature\": 7.15}," +
                "{ \"pointInTime\" :\"2021-10-30T00:01:00\", \"temperature\": 6.01}," +
                "{ \"pointInTime\" :\"2021-10-30T00:02:00\", \"temperature\": 6.02}," +
                "{ \"pointInTime\" :\"2021-10-30T00:03:00\", \"temperature\": 6.03}," +
                "{ \"pointInTime\" :\"2021-10-30T00:04:00\", \"temperature\": 6.04}," +
                "{ \"pointInTime\" :\"2021-10-30T00:05:00\", \"temperature\": 6.05}," +
                "{ \"pointInTime\" :\"2021-10-30T01:01:00\", \"temperature\": 6.11}," +
                "{ \"pointInTime\" :\"2021-10-30T01:02:00\", \"temperature\": 6.12}," +
                "{ \"pointInTime\" :\"2021-10-30T01:03:00\", \"temperature\": 6.13}," +
                "{ \"pointInTime\" :\"2021-10-30T01:04:00\", \"temperature\": 6.14}," +
                "{ \"pointInTime\" :\"2021-10-30T01:05:00\", \"temperature\": 6.15}," +
                "{ \"pointInTime\" :\"2021-10-31T00:01:00\", \"temperature\": 5.01}," +
                "{ \"pointInTime\" :\"2021-10-31T00:02:00\", \"temperature\": 5.02}," +
                "{ \"pointInTime\" :\"2021-10-31T00:03:00\", \"temperature\": 5.03}," +
                "{ \"pointInTime\" :\"2021-10-31T00:04:00\", \"temperature\": 5.04}," +
                "{ \"pointInTime\" :\"2021-10-31T00:05:00\", \"temperature\": 5.05}," +
                "{ \"pointInTime\" :\"2021-10-31T01:01:00\", \"temperature\": 5.11}," +
                "{ \"pointInTime\" :\"2021-10-31T01:02:00\", \"temperature\": 5.12}," +
                "{ \"pointInTime\" :\"2021-10-31T01:03:00\", \"temperature\": 5.13}," +
                "{ \"pointInTime\" :\"2021-10-31T01:04:00\", \"temperature\": 5.14}," +
                "{ \"pointInTime\" :\"2021-10-31T01:05:00\", \"temperature\": 5.15}" +
                "]}")
                .andExpect(status().isCreated());

        mvc.perform(get("/sensor/" + sensorId + "/temperature/hourly")
                        .param("from", "2021-10-29")
                        .param("fromHourOfTheDay", "1")
                        .param("to", "2021-10-31")
                        .param("toHourOfTheDay", "0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{" +
                                "  \"sensorId\": \"" + sensorId + "\"," +
                                "  \"from\": \"2021-10-29T01:00:00\"," +
                                "  \"to\": \"2021-10-31T00:00:00\"," +
                                "  \"data\": [" +
                                "    {" +
                                "      \"average\": 7.13," +
                                "      \"date\": \"2021-10-29T01:00:00\"" +
                                "    }," +
                                "    {" +
                                "      \"average\": 6.03," +
                                "      \"date\": \"2021-10-30T00:00:00\"" +
                                "    }," +
                                "    {" +
                                "      \"average\": 6.13," +
                                "      \"date\": \"2021-10-30T01:00:00\"" +
                                "    }," +
                                "    {" +
                                "      \"average\": 5.03," +
                                "      \"date\": \"2021-10-31T00:00:00\"" +
                                "    }" +
                                "  ]" +
                                "}"));
    }

    //TODO: ... here i would add other tests to check if contract/requestParams/etc
    // are properly validated at controller layer

    private ResultActions performPut(String sensorId, String content) throws Exception {
        return mvc.perform(put("/sensor/" + sensorId + "/temperature", content)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(content));
    }
}
