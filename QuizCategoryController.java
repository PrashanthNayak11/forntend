package com.vnrit.mykidsdrawing.controller;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import com.vnrit.mykidsdrawing.exception.UnauthorisedException;
import com.vnrit.mykidsdrawing.model.QuizCategory;
import com.vnrit.mykidsdrawing.repository.AdminRepository;
import com.vnrit.mykidsdrawing.repository.QuizCategoryRepository;
import com.vnrit.mykidsdrawing.repository.SchoolDetailsRepository;
import com.vnrit.mykidsdrawing.repository.UserRepository;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

@CrossOrigin(origins="*",allowedHeaders="*")
@RestController
public class QuizCategoryController {
	
	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private SchoolDetailsRepository schoolDetailsRepository;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private QuizCategoryRepository categoryRepository;
	@Autowired
	private AdminRepository adminRepository;
	
	@Autowired
	private QuizCategoryRepository quizCategoryRepository;
	
	@Value("${topicImage.upload-dir}")
	private String uploadPath;
	
	
	@PostMapping("/api/create-category")
	public QuizCategory storeFile(@RequestParam(name="file",required = false) MultipartFile file,@ModelAttribute QuizCategory category,HttpServletRequest request) 
	{
	
		
         String headerToken = request.getHeader("apiToken");
		
		int verifyapiToken = adminRepository.verifyapiTokens(headerToken);
		
		if(verifyapiToken == 0) {
			
			String error = "UnAuthorised Admin";
			String message = "Not Successful";
			
			throw new UnauthorisedException(401, error, message);
		}
//		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
		
		if(file!=null) {
			long unique = new Date().getTime();
			String fileName = unique + "-" + file.getOriginalFilename().replace(" ", "_");
			String thumbnailName = unique + "-thumbnail-" + file.getOriginalFilename().replace(" ", "_");
			OutputStream opStream = null;
			category.setFilename(fileName);
			category.setTumbnailName(thumbnailName);
			try {
				byte[] byteContent = file.getBytes();
				File myFile = new File(uploadPath + fileName); // destination path
				System.out.println("fileName is " + myFile);
				// check if file exist, otherwise create the file before writing
				if (!myFile.exists()) {
					myFile.createNewFile();
				}
				opStream = new FileOutputStream(myFile);
				opStream.write(byteContent);
				opStream.flush();
				opStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				File destinationDir = new File(uploadPath);
				Thumbnails.of(uploadPath + fileName).size(900, 800).toFiles(destinationDir, Rename.NO_CHANGE);
				Thumbnails.of(new File(uploadPath + fileName)).size(348, 235).toFile(uploadPath + thumbnailName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Boolean status = category.getStatus();
		Boolean isTopicComplete = category.getIsTopicComplete();
		
		category.setStatus(true);
		if(isTopicComplete==null) {
			category.setIsTopicComplete(false);
		}else {
			category.setIsTopicComplete(true);
		}
		quizCategoryRepository.save(category);
		return category;
		
	 
	}
	@GetMapping("/api/get-category")
	public List<Map> getdetailsCategory(){
		
		return categoryRepository.getdetailsCategory();
	
}
	
	@GetMapping("/api/getImage/{category}")
	public File getImage(@PathVariable String category) throws IOException
	{
			  String filename=quizCategoryRepository.getImage(category);
			  String destinationDir = uploadPath + filename;
			  System.out.println(filename);
			  File image = ResourceUtils.getFile(destinationDir);
			  return image;
	}
	
//	@PutMapping("/api/updateQuestion/{category}")
//	public QuizCategory updateQuestion(@RequestParam(name="file",required = false) MultipartFile file,@PathVariable String category,String updatedQuestion) throws FileStorageException, ResourceNotFoundException
//	{
//		String fileName = StringUtils.cleanPath(file.getOriginalFilename());
//		try {
//	           
//		       // Check if the file's name contains invalid characters
//	           if(fileName.contains("..") || fileName.isEmpty()) { 
//	        	   throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);	
//	               
//	           }
//	    }
//		catch(FileStorageException ex)
//		{
//			  throw new FileStorageException("Could not store file " + fileName + ". Please try again!");
//		}
//		long unique = new Date().getTime();
//		String tumbnailName = unique + "-thumbnail-" + file.getOriginalFilename().replace(" ", "_"); 
//		OutputStream opStream = null;
//		int id=quizCategoryRepository.getId(category);
//		QuizCategory selectedQuestion = quizCategoryRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Quwstion not found :: " + id));
//		selectedQuestion.setFilename(fileName);
//		selectedQuestion.setTumbnailName(tumbnailName);
//		Path path=Paths.get(uploadPath).toAbsolutePath().normalize();
//	    try {
//			byte[] byteContent = file.getBytes();
//			Files.createDirectories(path);
//			File myFile = new File(uploadPath + fileName);
//			// destination path
//			System.out.println("fileName is " + myFile);
//			// check if file exist, otherwise create the file before writing
//			if (!myFile.exists()) {
//				myFile.createNewFile();
//			}
//			opStream = new FileOutputStream(myFile);
//			opStream.write(byteContent);
//			opStream.flush();
//			opStream.close();
//		} 
//	    catch (IOException e) {
//			e.printStackTrace();
//		}
//		try {
//			File destinationDir = new File(uploadPath);
//			Thumbnails.of(uploadPath + fileName).size(900, 800).toFiles(destinationDir, Rename.NO_CHANGE);
//			Thumbnails.of(new File(uploadPath + fileName)).size(348, 235).toFile(uploadPath + tumbnailName);
//		} 
//		catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		selectedQuestion.setQuestion(updatedQuestion);
//		QuizCategory updatedcategory=quizCategoryRepository.save(selectedQuestion);
//		return updatedcategory;
//	}
	
	@PatchMapping("/api/update-topic/{id}")
	public QuizCategory updateQuizCategory(@PathVariable Long id,@RequestBody QuizCategory quizCategory) {
		
		String headerToken = request.getHeader("apiToken");
        int adminApiCount = adminRepository.verifyapiTokens(headerToken);
		
        //UnauthorisedException thrown if apiToken doesn't match admin ApiToken
		if( adminApiCount==0 ) {
			
			String error = "UnAuthorised User";
			String message = "Not Successful";
			
			throw new UnauthorisedException(401, error, message);
		}
		
		QuizCategory quizCategoryObj = quizCategoryRepository.getOne(id);
		String category = quizCategory.getCategory();
		String subcategory = quizCategory.getSubcategory();
		
		String grade = quizCategory.getGrade();
		Boolean status = quizCategory.getStatus();
		Boolean isTopicComplete = quizCategory.getIsTopicComplete();
		if(category!=null) {
			quizCategoryObj.setCategory(category);
		}else {
			quizCategoryObj.setCategory(quizCategoryObj.getCategory());
		}
		if(subcategory!=null) {
			quizCategoryObj.setSubcategory(subcategory);
		}else {
			quizCategoryObj.setSubcategory(quizCategoryObj.getSubcategory());
		}
		
		if(grade!=null) {
			quizCategoryObj.setGrade(grade);
		}else {
			quizCategoryObj.setGrade(quizCategoryObj.getGrade());
		}
		if(status!=null) {
			quizCategoryObj.setStatus(status);
		}else {
			quizCategoryObj.setStatus(quizCategoryObj.getStatus());
		}
		if(isTopicComplete!=null) {
			quizCategoryObj.setIsTopicComplete(isTopicComplete);
		}else {
			quizCategoryObj.setIsTopicComplete(quizCategoryObj.getIsTopicComplete());
		}
		quizCategoryRepository.save(quizCategoryObj);
		return quizCategoryObj;
		
	}
	
	@DeleteMapping("/api/delete-quiz-categorys/{id}")
	public String deleteQuizCategory(@PathVariable Long id) {
		
		String headerToken = request.getHeader("apiToken");
        int adminApiCount = adminRepository.verifyapiTokens(headerToken);
		
        //UnauthorisedException thrown if apiToken doesn't match admin ApiToken
		if( adminApiCount==0 ) {
			
			String error = "UnAuthorised User";
			String message = "Not Successful";
			
			throw new UnauthorisedException(401, error, message);
		}
		
		String filename = quizCategoryRepository.getFilenameById(id);
		quizCategoryRepository.deletecategory(id);
		
		 String destinationDir = uploadPath + filename;
		 try { 
             File file = new File(destinationDir);
             if(file.delete()) { 
                System.out.println(file.getName() + " is deleted!");
             } else {
                System.out.println("Delete operation is failed.");
                }
          }
            catch(Exception e)
            {
                System.out.println("Failed to Delete image !!");
            }
		 return "Successfully Deleted";
	}

}
