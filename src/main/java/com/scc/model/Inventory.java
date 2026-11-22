package com.scc.model;

public record Inventory(int inventory_id, String container_type, int quantity_full, int quantity_empty) {
}
