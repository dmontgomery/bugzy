package in.bugzy.data.remote.model;

public class Error {
   protected String message;
   protected String detail;
   protected String code;

   public Error() {

   }

   public Error(String message, String detail, String code) {
      this.message = message;
      this.detail = detail;
      this.code = code;
   }

   public String getMessage() {
      return message;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getDetail() {
      return detail;
   }

   public void setDetail(String detail) {
      this.detail = detail;
   }

   public String getCode() {
      return code;
   }

   public void setCode(String code) {
      this.code = code;
   }

   public void setCode(int code) {
      this.code = code + "";
   }
}
