package com.in.jplearning.serviceImpl;

import com.in.jplearning.config.JwtAuthFilter;
import com.in.jplearning.config.VNPayConfig;
import com.in.jplearning.constants.JPConstants;
import com.in.jplearning.model.Bill;
import com.in.jplearning.model.Premium;
import com.in.jplearning.model.User;
import com.in.jplearning.repositories.BillDAO;
import com.in.jplearning.repositories.PremiumDAO;
import com.in.jplearning.repositories.UserDAO;
import com.in.jplearning.service.VNPayService;
import com.in.jplearning.utils.JPLearningUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@AllArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final PremiumDAO premiumDAO;

    private final BillDAO billDAO;



    private final UserDAO userDAO;

    private final JwtAuthFilter jwtAuthFilter;
    @Override
    public ResponseEntity<?> createPayment(Long premiumID) {
        try{
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String orderType = "other";
            String bankCode = "NCB";
            //get premium type
            Premium premium = premiumDAO.findById(premiumID).get();

            long amount = premium.getPrice() * 100;
            String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
            String vnp_IpAddr = "127.0.0.1";

            String vnp_TmnCode = VNPayConfig.vnp_TmnCode;

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount));
            vnp_Params.put("vnp_CurrCode", "VND");

            vnp_Params.put("vnp_BankCode", bankCode);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", "Thanh toan:" + vnp_TxnRef);
            vnp_Params.put("vnp_OrderType", orderType);

            vnp_Params.put("vnp_Locale", "vn");
            vnp_Params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl + "?premiumID=" + premiumID);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            List fieldNames = new ArrayList(vnp_Params.keySet());
            Collections.sort(fieldNames);
            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();
            Iterator itr = fieldNames.iterator();
            while (itr.hasNext()) {
                String fieldName = (String) itr.next();
                String fieldValue = (String) vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    //Build hash data
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    //Build query
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    query.append('=');
                    query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    if (itr.hasNext()) {
                        query.append('&');
                        hashData.append('&');
                    }
                }
            }
            String queryUrl = query.toString();
            String vnp_SecureHash = VNPayConfig.hmacSHA512(VNPayConfig.secretKey, hashData.toString());
            queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
            String paymentUrl = VNPayConfig.vnp_PayUrl + "?" + queryUrl;
           return JPLearningUtils.getResponseEntity(paymentUrl,HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> paymentCallBack(Map<String, String> requestMap) {
        try{
            String vnp_ResponseCode = requestMap.get("vnp_ResponseCode");
            // get premiumID
            String premiumIDRaw = requestMap.get("premiumID");
            //check if premiumid is exist
            if(premiumIDRaw !=null && !premiumIDRaw.equals("")){
                //check if payment sucess
                if("00".equals(vnp_ResponseCode)){
                    //save into bill
                    Bill bill = generateBill(premiumIDRaw);
                    billDAO.save(bill);
                    return JPLearningUtils.getResponseEntity("Thanh toán thành công", HttpStatus.OK);
                }
            } else {
                return JPLearningUtils.getResponseEntity("Thanh toán thất bại", HttpStatus.OK);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return JPLearningUtils.getResponseEntity(JPConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Bill generateBill(String premiumIDRaw) {
        Premium premium = premiumDAO.findById(Long.parseLong(premiumIDRaw)).get();
        User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).get();
         Date currentDate = getDate();
        //generate bill number
        String billNumber = UUID.randomUUID().toString().replaceAll("-", "");
        Bill bill =Bill.builder()
                .billNumber(billNumber)
                .paymentMethod("VNPAY")
                .premium(premium)
                .user(user)
                .createdAt(currentDate)
                .total(premium.getPrice())
                .build();
        return bill;
    }

    private Date getDate() {
        LocalDate currentDate = LocalDate.now();
        return Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

    }
}
