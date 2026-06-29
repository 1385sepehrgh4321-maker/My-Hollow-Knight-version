package com.Sepehr.HallowKnight.Menu;

public enum CharmType {
    QUICK_SLASH("charms/Quick Slash - _0003_charm_nail_slash_speed_up.png"),
    UNBREAKABLE_STRENGTH("charms/Unbreakable Strength_0002_charm_glass_attack_up_full.png"),
    DASHMASTER("charms/Dashmaster - _0011_charm_generic_03.png"),
    SOUL_CATCHER("charms/Soul Catcher - _0001_charm_more_soul.png"),
    VOID_HEART("charms/Void Heart - charm_black.png"),
    SHARP_SHADOW("charms/Sharp Shadow - charm_shade_impact.png"),
    HEAVY_BLOW("charms/Heavy Blow - _0008_charm_nail_damage_up.png"),
    QUICK_FOCUS("charms/Quick Focus - _0005_charm_fast_focus.png");

    private final String path;

    CharmType(String path) {
        this.path = path;
    }

    public String getPath() {
        return this.path;
    }
}
