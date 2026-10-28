package io.github.baruth91.data;

import java.util.UUID;

/** Immutable synthetic input; no real person or private contact information. */
public record OwnerData(String firstName, String lastName, String address, String city, String telephone) {
    public static OwnerData unique() {
        return new OwnerData("Learning", "Qa" + UUID.randomUUID().toString().replace("-", "").substring(0, 12),
                "100 Learning Lane", "Example City", "2025550148");
    }

    public OwnerData withTelephone(String phone) {
        return new OwnerData(firstName, lastName, address, city, phone);
    }

    public OwnerData withFirstName(String name) {
        return new OwnerData(name, lastName, address, city, telephone);
    }
}
