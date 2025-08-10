package lunar.tinkerer;

import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.MathHelper;


public class ManathiefFaceModel extends Model {
    private final ModelPart leftCover;
    private final ModelPart rightCover;
    private final ModelPart leftPages;
    private final ModelPart rightPages;
    private final ModelPart leftFlippingPage;
    private final ModelPart rightFlippingPage;

    public ManathiefFaceModel(ModelPart root) {
        super(root, RenderLayer::getEntitySolid);
        this.leftCover = root.getChild("left_lid");
        this.rightCover = root.getChild("right_lid");
        this.leftPages = root.getChild("left_pages");
        this.rightPages = root.getChild("right_pages");
        this.leftFlippingPage = root.getChild("flip_page1");
        this.rightFlippingPage = root.getChild("flip_page2");
    }

    public void setPageAngles(float pageTurnAmount, float leftFlipAmount, float rightFlipAmount, float pageTurnSpeed) {
        float f = (MathHelper.sin(pageTurnAmount * 0.02F) * 0.1F + 1.25F) * pageTurnSpeed;
        this.leftCover.yaw = (float)Math.PI + f;
        this.rightCover.yaw = -f;
        this.leftPages.yaw = f;
        this.rightPages.yaw = -f;
        this.leftFlippingPage.yaw = f - f * 2.0F * leftFlipAmount;
        this.rightFlippingPage.yaw = f - f * 2.0F * rightFlipAmount;
        this.leftPages.originX = MathHelper.sin(f);
        this.rightPages.originX = MathHelper.sin(f);
        this.leftFlippingPage.originX = MathHelper.sin(f);
        this.rightFlippingPage.originX = MathHelper.sin(f);
    }
}
