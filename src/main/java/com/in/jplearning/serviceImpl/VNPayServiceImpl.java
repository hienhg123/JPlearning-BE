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
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@AllArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private final PremiumDAO premiumDAO;

    private final BillDAO billDAO;



    private final UserDAO userDAO;

    private final JwtAuthFilter jwtAuthFilter;
    @Override
    public ResponseEntity<?> createPayment(Long premiumID) {
        try{
            //check if user have already bought this premium or the current one is expired
            if(checkPremium(premiumID)){
                return JPLearningUtils.getResponseEntity("Bạn không thể mua thêm khóa hiện tại hoặc khóa thấp hơn khi chưa hết hạn",HttpStatus.BAD_REQUEST);
            }
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
    public ResponseEntity<String> paymentCallBack(Map<String, String> requestMap, HttpServletResponse response) {
        try{
            String vnp_ResponseCode = requestMap.get("vnp_ResponseCode");
            log.info(vnp_ResponseCode);
            // get premiumID
            String premiumIDRaw = requestMap.get("premiumID");
            //check if premiumid is exist
            if(premiumIDRaw !=null && !premiumIDRaw.equals("")){
                //check if payment sucess
                if("00".equals(vnp_ResponseCode)){
                    //save into bill
                    Bill bill = generateBill(premiumIDRaw);
                    billDAO.save(bill);
                    response.sendRedirect("http://localhost:4200/checkout-success");
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
    private boolean checkPremium(Long premiumID) {

        Premium premium = premiumDAO.findById(premiumID).get();
        //get the current user premium
        List<Bill> bills = billDAO.getbyUser(jwtAuthFilter.getCurrentUser());
        //check if user havd ever bought premium
        if(bills.isEmpty()){
            return false;
        }
        Bill lastBill = new Bill();
        //get the latest bill
        if(bills.size() == 1){
            lastBill = bills.get(bills.size());
        }
        lastBill = bills.get(bills.size() -1 );
        //check the current bill
        if(lastBill.getPremium().getPremiumID() == premiumID){
            LocalDate startDate = lastBill.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate expirationDate = startDate.plusMonths(premium.getDuration());
            LocalDate currentDate = LocalDate.now();
            //check date
            if(currentDate.isAfter(expirationDate)){
                return false;
            }
            return true;
        }
        //check if the current premium id is larget
        if(lastBill.getPremium().getPremiumID() > premiumID){
            return false;
        } else{
            return true;
        }
    }
    @Override
    public ResponseEntity<Page<Map<String, Object>>> getBillHistoryByUser(int pageNumber, int pageSize) {
        try {
            // Get the current user
            User user = userDAO.findByEmail(jwtAuthFilter.getCurrentUser()).orElse(null);
            if (user != null) {
                log.info("User ID: " + user.getUserID());
                // Get bill history by user ID with pagination
                Page<Bill> billHistoryPage = billDAO.findByUserEmail(user.getEmail(), PageRequest.of(pageNumber, pageSize));

                // Convert Page<Bill> to Page<Map<String, Object>> containing bill information maps
                Page<Map<String, Object>> billInfoPage = billHistoryPage.map(bill -> {
                    Map<String, Object> billInfoMap = new HashMap<>();
                    billInfoMap.put("duration", bill.getPremium().getDuration());
                    billInfoMap.put("total", bill.getTotal());
                    billInfoMap.put("createdAt", bill.getCreatedAt());
                    // You can add more properties to the map if needed
                    return billInfoMap;
                });

                // Return the paged bill information
                return ResponseEntity.ok(billInfoPage);
            } else {
                // User not found
                log.error("User not found.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception ex) {
            log.error("Error occurred while fetching bill history for user.", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    private Date getDate() {
        LocalDate currentDate = LocalDate.now();
        return Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }


}
