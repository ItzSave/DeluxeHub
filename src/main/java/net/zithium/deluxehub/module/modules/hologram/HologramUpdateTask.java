package net.zithium.deluxehub.module.modules.hologram;

public class HologramUpdateTask implements Runnable {

    private final HologramManager hologramManager;

    public HologramUpdateTask(HologramManager hologramManager) {
        this.hologramManager = hologramManager;
    }

    @Override
    public void run() {
        for (Hologram hologram : hologramManager.getHolograms()) {
            hologram.updatePlaceholders();
        }
    }
}
