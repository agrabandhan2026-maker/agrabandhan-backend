package com.agrabandhan.common.entity;

/**
 * The 18 recognized gotras of the Bisa Aggarwal Baniya community.
 * This enum is the single source of truth for gotra validation.
 * Same-gotra matching is a HARD rejection rule enforced at the query level.
 */
public enum Gotra {
    AIRAN("Airan"),
    BANSAL("Bansal"),
    BHANDAL("Bhandal"),
    BINDAL("Bindal"),
    DHARAN("Dharan"),
    GARG("Garg"),
    GOYAL("Goyal"),
    GOYAN("Goyan"),
    JINDAL("Jindal"),
    KANSAL("Kansal"),
    KUCHHAL("Kuchhal"),
    MADHUKUL("Madhukul"),
    MANGAL("Mangal"),
    MITTAL("Mittal"),
    NAGIL("Nagil"),
    SINGHAL("Singhal"),
    TAYAL("Tayal"),
    TINGAL("Tingal");

    private final String displayName;

    Gotra(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
