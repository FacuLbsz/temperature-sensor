package faka.cs.tsensor.adapters.persistence.entity;

import lombok.Value;

@Value
public class SensorActivityEntity {
    Integer minute;
    Double temperature;
}
