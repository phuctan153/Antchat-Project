package fu.se170572.antchat.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        // Kiểm tra file rỗng
        if (file.isEmpty()) {
            throw new RuntimeException("File tải lên không được để trống!");
        }

        // Upload lên Cloudinary. "auto" giúp tự động nhận diện đó là ảnh, video hay file thô
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto"));

        // Trả về đường dẫn URL an toàn (HTTPS)
        return uploadResult.get("secure_url").toString();
    }
}
