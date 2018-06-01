package ch.ethz.sis.benchmark;

import java.util.HashMap;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class BenchmarkConfig
{
    private String className;
    private String user;
    private String password;
    private String openbisURL;
    private int openbisTimeout;
    private String datastoreURL;
    private int datastoreTimeout;
    private HashMap<String, String> parameters;
    private int threads;
}
