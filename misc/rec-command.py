#!/usr/bin/env python

import urllib, urllib2, argparse, json

default_url = "http://127.0.0.1:3001/api/2.0/"

def parse_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('-u', '--url', nargs='?', type=str, default=None)
    parser.add_argument('-l', '--location', nargs='?', type=str, default=None)
    parser.add_argument('-t', '--type', nargs='?', type=str, default=None)
    parser.add_argument('-d', '--data', nargs='?', type=int, default=None)
    parser.add_argument('-m', '--method', nargs='?', type=str, default=None)
    args = parser.parse_args();
    return args

def apply_args(d, args):
    dict_args = vars(args)
    for key in dict_args:
        if dict_args[key] != None:
            d[key] = dict_args[key]

def main():
    params = {
        "location" : "UnKnown",
        "type" : "UnKnown",
        "data" : "1",
        "authid" : "JhonDoe@dummy",
        }
    rpc = {
        "jsonrpc" : "2.0",
        "method" : "pushRawData", # FIX ME!!
        "params" : None,
        "id" : 0,
        }

    args = parse_args()
    apply_args(params, args)
    params = params # FIX ME
    rpc["params"] = params
    if args.method != None:
        rpc["method"] = args.method
    #print(rpc)
    req_data = json.dumps(rpc);

    header = {
        "Content-Type" : "application/json",
        "Accept" : "application/json",
        }
    try:
        req = urllib2.Request(default_url, req_data, header)
        response = urllib2.urlopen(req)
        res_raw = response.read()
        res_json = json.loads(res_raw)
        #print(res_json)
        if "result" in res_json:
            if res_json["result"] != None:
                print(res_json["result"]["data"])
            else:
                print(0)
        else:
            print(0)
    except urllib2.URLError as e:
        #print(e)
        print(e.read())

if __name__ == "__main__":
    main()
