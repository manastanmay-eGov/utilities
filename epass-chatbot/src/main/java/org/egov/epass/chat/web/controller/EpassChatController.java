package org.egov.epass.chat.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.egov.tracer.kafka.CustomKafkaTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
public class EpassChatController {

    @Autowired
    private KafkaTemplate<String, JsonNode> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private String receivedMessageTopicName = "karix-received-messages";

    @RequestMapping(value = "/messages", method = RequestMethod.GET)
    public ResponseEntity<JsonNode> receiveMessage(@RequestParam Map<String, String> params) throws Exception {
        log.info("Received sms from user : " + params.toString());

        String mobileNumber = params.get("sender");
        String messageContent = params.get("msg");
        String recipientNumber = params.get("dest");
        String stime = params.get("stime");

        ObjectNode extraInfo = objectMapper.createObjectNode();
        extraInfo.put("stime", stime);
        extraInfo.put("recipient", recipientNumber);

        ObjectNode chatNode = objectMapper.createObjectNode();
        chatNode.put("timestamp", System.currentTimeMillis());
        chatNode.put("mobileNumber", mobileNumber);
        chatNode.put("messageContent", messageContent);
        chatNode.set("extraInfo", extraInfo);

        kafkaTemplate.send(receivedMessageTopicName, mobileNumber, chatNode);

        return new ResponseEntity<>(createResponse(), HttpStatus.OK);
    }

    private JsonNode createResponse() {
        ObjectNode response = objectMapper.createObjectNode();
        response.put("id", UUID.randomUUID().toString());
        return response;
    }

}
