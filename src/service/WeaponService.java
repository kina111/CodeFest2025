package service;

import jsclub.codefest.sdk.model.weapon.Weapon;

import java.util.HashMap;
import java.util.Map;

public class WeaponService {
    private static final WeaponService instance = new WeaponService();
    private final Map<String, Long> cooldownMap = new HashMap<>();
    private static final float DELAYDISAPPEAR = 0.5F;

    private WeaponService() {}

    public static WeaponService getInstance() {
        return instance;
    }

    public boolean canUse(Weapon weapon, long currentStep) {
        String id = weapon.getId();
        return !cooldownMap.containsKey(id)
                || currentStep - cooldownMap.get(id) >= getCooldown(weapon) + DELAYDISAPPEAR;
    }

    public void markUsed(String weapon, long currentStep) {
        cooldownMap.put(weapon, currentStep);
    }

    private long getCooldown(Weapon weapon) {
        // ví dụ: mỗi vũ khí 5 step
        return (long) weapon.getCooldown();
    }
}
