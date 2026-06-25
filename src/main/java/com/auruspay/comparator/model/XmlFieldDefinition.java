package com.auruspay.comparator.model;

public class XmlFieldDefinition {
    private String id;
    private String name;
    private String classType;
    private String pattern;
    private String failedMsg;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClassType() { return classType; }
    public void setClassType(String classType) { this.classType = classType; }
    public String getPattern() { return pattern; }
    public void setPattern(String pattern) { this.pattern = pattern; }
    public String getFailedMsg() { return failedMsg; }
    public void setFailedMsg(String failedMsg) { this.failedMsg = failedMsg; }
}