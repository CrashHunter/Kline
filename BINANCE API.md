**https://api.binance.com**

/sapi/v1/accountSnapshot







- 调用`SIGNED` 接口时，除了接口本身所需的参数外，还需要在`query string` 或 `request body`中传递 `signature`, 即签名参数。
- 签名使用`HMAC SHA256`算法. API-KEY所对应的API-Secret作为 `HMAC SHA256` 的密钥，其他所有参数作为`HMAC SHA256`的操作对象，得到的输出即为签名。
- `签名` **大小写不敏感**.
- "totalParams"定义为与"request body"串联的"query string"。







| apiKey    | vmPUZE6mv9SD5VNHk4HlWFsOr6aKE2zvsw0MuIgwCIPy6utIco14y7Ju91duEh8A |
| --------- | ------------------------------------------------------------ |
| secretKey | NhqPtmdSJYdKjVHjA7PZj4Mge3R5YNiP1e3UZjInClVN65XAbvqqM6A7H5fATj0j |

```
    $ echo -n "symbol=LTCBTC&side=BUY&type=LIMIT&timeInForce=GTC&quantity=1&price=0.1&recvWindow=5000&timestamp=1499827319559" | openssl dgst -sha256 -hmac "NhqPtmdSJYdKjVHjA7PZj4Mge3R5YNiP1e3UZjInClVN65XAbvqqM6A7H5fATj0j"
    (stdin)= c8db56825ae71d6d79447849e617115f4a920fa2acdcab2b053c4b2838bd6b71
```



```
 (HMAC SHA256)
    $ curl -H "X-MBX-APIKEY: vmPUZE6mv9SD5VNHk4HlWFsOr6aKE2zvsw0MuIgwCIPy6utIco14y7Ju91duEh8A" -X POST 'https://api.binance.com/api/v3/order' -d 'symbol=LTCBTC&side=BUY&type=LIMIT&timeInForce=GTC&quantity=1&price=0.1&recvWindow=5000&timestamp=1499827319559&signature=c8db56825ae71d6d79447849e617115f4a920fa2acdcab2b053c4b2838bd6b71'
```







```
public static final String API_KEY = "GgCepbqNzYIamQ7MOqDDsqmfYHumKR2JIbhDWIhtNSL4otydg3doK7lgAFYhUraq";
public static final String SECRET_KEY = "nTPlk3G14fbvzwprJiovVzb8FvYGz6EqogD0M3O5XevR7FcZQkD5KYuPORSlbRli";
```



```
    $ echo -n "type=SPOT&timestamp=" | openssl dgst -sha256 -hmac "nTPlk3G14fbvzwprJiovVzb8FvYGz6EqogD0M3O5XevR7FcZQkD5KYuPORSlbRli"
    
    430c710dc488bcb82b5be5039faa67dffc1b5a5d69d307230ac4e979f7ce0d88
```



```
 (HMAC SHA256)
    $ curl -H "X-MBX-APIKEY: GgCepbqNzYIamQ7MOqDDsqmfYHumKR2JIbhDWIhtNSL4otydg3doK7lgAFYhUraq" -X GET 'https://api.binance.com/sapi/v1/accountSnapshot' -d 'type=SPOT&signature=430c710dc488bcb82b5be5039faa67dffc1b5a5d69d307230ac4e979f7ce0d88'
    
    
    $ curl -H "X-MBX-APIKEY: GgCepbqNzYIamQ7MOqDDsqmfYHumKR2JIbhDWIhtNSL4otydg3doK7lgAFYhUraq" -X POST 'https://api.binance.com/sapi/v1/accountSnapshot' -d 'type=SPOT&signature=430c710dc488bcb82b5be5039faa67dffc1b5a5d69d307230ac4e979f7ce0d88'
```



```
RestApiRequest<JSONObject> getPositionSide() {
```



```

```

