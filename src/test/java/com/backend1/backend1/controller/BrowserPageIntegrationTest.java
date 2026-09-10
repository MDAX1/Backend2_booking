package com.backend1.backend1.controller;

import com.backend1.backend1.dto.RoomDTO;
import com.backend1.backend1.model.RoomType;
import com.backend1.backend1.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BrowserPageIntegrationTest {
    @Autowired TestRestTemplate http;
    @MockitoBean RoomService rooms;

    @Test
    void firstVisitRendersTheFullRoomPageAndCreatesCsrfSessionBeforeResponseIsCommitted() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(5000);
        http.getRestTemplate().setRequestFactory(factory);
        RoomDTO room = new RoomDTO();
        room.setId(1L);
        room.setRoomNumber("K8S-DEMO");
        room.setType(RoomType.DOUBLE);
        room.setCapacity(2);
        room.setPricePerNight(BigDecimal.valueOf(995));
        room.setTypeDescription("Dubbelrum");
        when(rooms.findAll()).thenReturn(List.of(room));

        // Use a real server: MockMvc does not enforce Tomcat's restriction on
        // creating a session after a large HTML response has already been flushed.
        var response = http.getForEntity("/rooms", String.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)).contains("JSESSIONID=");
        assertThat(response.getBody()).contains("K8S-DEMO", "name=\"_csrf\"", "</html>");
    }
}
