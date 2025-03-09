# DiscountService

## Overview
DiscountService is a service designed to manage and apply discounts to various products or services. It provides a set of API to apply discounts on carts , and retrieve discount information.


## Prerequisites 
* Java 21
* Maven


## Database 
* sqlite ( for simplicity,no setup needed)


## Features
- Create new discounts


## Installation
To install the DiscountService, follow these steps:

1. Clone the repository:
    ```sh
    https://github.com/rinshadkv/unifize.git
    ```
2. Navigate to the project directory:
    ```sh
    cd DiscountService
    ```
3. Install the dependencies:
    ```
   mvn clean install 
    ```
4. Run Test
    ```sh
    mvn test
    ```
5. run appliction
    ```sh
    mvn run
    ```


## API Endpoints
- `POST /customer/customerId/checkout` - Apply  discounts on cart for  customer

curl
```
POST /customer/2/checkout HTTP/1.1
Host: localhost:8080
Content-Type: application/json
Authorization: Basic YWRtaW46YWRtaW4=
Cookie: better-auth.session_token=rxdUZnRY0q3ay3aXN7Vs03fZzTfck0th.aLWYH1DrT%2B5lvNLdXuHTiT3ihcO7vIJfQ8Lrwymc9%2Fc%3D
Content-Length: 76

{
    "bankName":"ICICI",
    "cardType":"CREDIT",
    "method":"CARD"
}



```


## Add new data

- to  add new data goto  dataIntilaizer code, add more data 


## add new Test
-  to add new test go to  inside test folder and add new test 



## Explantion
`
This is a simple demo of a discount service and only focus in main functionality, not all edge case coverd only basic .  I followed given documentation as base . I konw lots thing missing from a real discount service. This project only intent to showcase my ability in lld and hld . 


