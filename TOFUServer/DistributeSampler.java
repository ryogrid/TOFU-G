import java.io.Serializable;

import raja.*;
import raja.renderer.*;

import java.awt.image.BufferedImage;
import javax.swing.BoundedRangeModel;

public class DistributeSampler implements Sampler, Serializable {
private AssignedPartDescriptor assignedPart;
private SubimageResult result;
private int antiAliasLevel;
        
        public DistributeSampler(int antialiasLevel){
            this.antiAliasLevel = antialiasLevel;
        }
        
        public void setAssignedPart(AssignedPartDescriptor part){
            this.assignedPart = part;
        }
        
        public void compute(Camera camera, RayTracer rt,BoundedRangeModel dummy, BufferedImage image)
        {
            if (image == null) {
                throw new IllegalArgumentException("null image");
            }
            
            result = new SubimageResult(0,image.getWidth(),0,image.getHeight());
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            long horizonalEnd = assignedPart.horizonalStart + assignedPart.horizonalWidth -1;
            long verticalEnd = assignedPart.verticalStart + assignedPart.verticalWidth -1;
//            for (long i = assignedPart.horizonalStart ; i <= horizonalEnd ; i++)
//            {
//                for (long j = assignedPart.verticalStart ; j <= verticalEnd ; j++)
//                {
//                    Ray ray = camera.getRay(((double) i + 0.5) / width,
//                                            ((double) j + 0.5) / height);
//                    RGB light = rt.getLight(ray);
//                    result.setColor((int)i,(int) j, light);
//                }
//            }
            
            
            double decalX = 1.0 / (antiAliasLevel * width);
            double decalY = 1.0 / (antiAliasLevel * height);

            for (long i = assignedPart.horizonalStart ; i < horizonalEnd ; i++)
            {
                for (long j = assignedPart.verticalStart ; j < verticalEnd ; j++)
                {
                    double px = (0.5 * decalX) + (((double) i) / width);
                    double py = (0.5 * decalY) + (((double) j) / height);

                    RGB light = RGB.black;

                    for (int kx = 0 ; kx < antiAliasLevel ; kx++)
                    {
                        for (int ky = 0 ; ky < antiAliasLevel ; ky++)
                        {
                            Ray ray = camera.getRay(px + (kx * decalX),
                                                    py + (ky * decalY));
                            light = RGB.sum(light, rt.getLight(ray));
                        }
                    }

                    light = RGB.product(light, 1.0 / (antiAliasLevel * antiAliasLevel));
                    result.setColor((int)i,(int) j, light);
                }
            }
        }
        
        public void compute(Camera camera, RayTracer rt,BufferedImage image)
        {
            if (image == null) {
                throw new IllegalArgumentException("null image");
            }
            
            result = new SubimageResult(0,image.getWidth(),0,image.getHeight());
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            long horizonalEnd = assignedPart.horizonalStart + assignedPart.horizonalWidth -1;
            long verticalEnd = assignedPart.verticalStart + assignedPart.verticalWidth -1;
//            for (long i = assignedPart.horizonalStart ; i <= horizonalEnd ; i++)
//            {
//                for (long j = assignedPart.verticalStart ; j <= verticalEnd ; j++)
//                {
//                    Ray ray = camera.getRay(((double) i + 0.5) / width,
//                                            ((double) j + 0.5) / height);
//                    RGB light = rt.getLight(ray);
//                    result.setColor((int)i,(int) j, light);
//                }
//            }
            
            
            double decalX = 1.0 / (antiAliasLevel * width);
            double decalY = 1.0 / (antiAliasLevel * height);

            for (long i = assignedPart.horizonalStart ; i < horizonalEnd ; i++)
            {
                for (long j = assignedPart.verticalStart ; j < verticalEnd ; j++)
                {
                    double px = (0.5 * decalX) + (((double) i) / width);
                    double py = (0.5 * decalY) + (((double) j) / height);

                    RGB light = RGB.black;

                    for (int kx = 0 ; kx < antiAliasLevel ; kx++)
                    {
                        for (int ky = 0 ; ky < antiAliasLevel ; ky++)
                        {
                            Ray ray = camera.getRay(px + (kx * decalX),
                                                    py + (ky * decalY));
                            light = RGB.sum(light, rt.getLight(ray));
                        }
                    }

                    light = RGB.product(light, 1.0 / (antiAliasLevel * antiAliasLevel));
                    result.setColor((int)i,(int) j, light);
                }
            }
        }

        public SubimageResult getResult() {
            return result;
        }
}
