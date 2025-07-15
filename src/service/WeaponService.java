package service;

import jsclub.codefest.sdk.model.ElementType;
import jsclub.codefest.sdk.model.weapon.Weapon;

import java.util.HashMap;
import java.util.Map;

public class WeaponService {
    private static final WeaponService instance = new WeaponService();
    private final Map<String, Float> cooldownMap = new HashMap<>();

    private WeaponService() {}

    public static WeaponService getInstance() {
        return instance;
    }

    public boolean canUse(Weapon weapon, float currentStep) {
        String id = weapon.getId();
        if (weapon.getType() == ElementType.THROWABLE) return true;
        return !cooldownMap.containsKey(id)
                || currentStep - cooldownMap.get(id) >= getCooldown(weapon);
    }

    public void markUsed(String weapon, float currentStep) {
        cooldownMap.put(weapon, currentStep);
    }

    private float getCooldown(Weapon weapon) {
        // ví dụ: mỗi vũ khí 5 step
        return (float) weapon.getCooldown();
    }
}
