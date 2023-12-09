package es.in2.blockchainconnector;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Test {

    public String variable; // declaració
    public String variable2 = "hola"; // declaració i inicialització

    public Test() {
        this.variable = "hola";
    }

    public void fesUnaLlista() {
        List<String> llista = new ArrayList<>();
        llista.add("hola");
        postRequest(
                "http://localhost:1026/ngsi-ld/v1/entities",
                "{...}", llista);
    }

    public void postRequest(String url, String body, List<String> headers) {
        // ...
    }


}
