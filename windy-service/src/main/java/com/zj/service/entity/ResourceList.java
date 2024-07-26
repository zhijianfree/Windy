package com.zj.service.entity;

import lombok.Data;

import java.util.List;

@Data
public class ResourceList {
    private String kind;

    private List<Resource> items;

    @Data
    public static class Resource {
        private MetaData metadata;
        private Status status;
    }

    @Data
    public static class MetaData{
        private String name;
    }

    @Data
    public static class Status{
        private List<Entry> addresses;
    }

    @Data
    public static class Entry{
        private String type;

        private String address;
    }
}
