while True:
	a=0
	b=0
	maxwatchtime=input("Video length : ")

	try:
		val=int(maxwatchtime)
		if(maxwatchtime.isdigit()):
			print("Your video length is set at :",val,"seconds")
			val=maxwatchtime
			a=maxwatchtime
			break;
		else:
			print("Please enter a positive number")
			continue;
		break;
	except ValueError:
		print("Please enter a digital number")
		continue;

while True:
	minwatchtime=input("Minimum watch time : ")
	try:
		val=int(minwatchtime)
		if(minwatchtime.isdigit()):
			print("Your minimum watch time is set at :",val,"seconds")
			val=minwatchtime
			b=minwatchtime
			break;
		else:
			print("Please enter a positive number")
			continue;
		break;
	except ValueError:
		print("Please enter a digital number")
		continue;

while a<=b :
	print("Please set the minimum watch time lower than the video length")
	while True:
		a=0
		b=0
		maxwatchtime=input("Video length : ")
		try:
			val=int(maxwatchtime)
			if(maxwatchtime.isdigit()):
				print("Your video length is set at :",val,"seconds")
				val=maxwatchtime
				a=maxwatchtime
				break;
			else:
				print("Please enter a positive number")
				continue;
			break;
		except ValueError:
			print("Please enter a digital number")
			continue;

	while True:
		minwatchtime=input("Minimum watch time : ")
		try:
			val=int(minwatchtime)
			if(minwatchtime.isdigit()):
				print("Your minimum watch time is set at :",val,"seconds")
				val=minwatchtime
				b=minwatchtime
				break;
			else:
				print("Please enter a positive number")
				continue;
			break;
		except ValueError:
			print("Please enter a digital number")
			continue;
		continue;
	break;	

print("Perfect ! Video length is set at : ",maxwatchtime," second and minimum watch time is set at : ",minwatchtime," seconds")

vmaxwatchtime=int(maxwatchtime)
vminwatchtime=int(minwatchtime)


while True:
	views=input("Number of views : ")
	try:
		val=int(views)
		if(views.isdigit()):
			print("Your number of views is set at :",val)
			val=views
			break;
		else:
			print("Please enter a positive number")
			continue;
		break;
	except ValueError:
		print("Please enter a digital number")
		continue;

vviews=int(views)

video_url=input("Youtube URL : ")

while video_url.startswith("https://www.youtube.com/watch?") == False :
	print("Please set a valid Youtube URL")
	video_url=input()
	continue;

print("The URL of your YouTube video is set as : ",video_url)


import random
import time

from random import randrange
from selenium import webdriver

import proxyscrape
from proxyscrape import create_collector, get_collector

refresh_time=random.randint(vminwatchtime,vmaxwatchtime)


browser_list=[]
browser_one = webdriver.Chrome(r"C:\Users\Julien MAUCLAIR\Desktop\Bot Youtube\webdriver\chromedriver")
browser_two = webdriver.Chrome(r"C:\Users\Julien MAUCLAIR\Desktop\Bot Youtube\webdriver\chromedriver")
browser_three = webdriver.Chrome(r"C:\Users\Julien MAUCLAIR\Desktop\Bot Youtube\webdriver\chromedriver")
browser_four = webdriver.Chrome(r"C:\Users\Julien MAUCLAIR\Desktop\Bot Youtube\webdriver\chromedriver")

browser_list.append(browser_one)
browser_list.append(browser_two)
browser_list.append(browser_three)
browser_list.append(browser_four)

for browser in browser_list:
	browser.get(video_url)

collector = create_collector('proxy-collector', 'https')

a=0

while a < vviews :

	while(True):

		
		proxy = collector.get_proxy()
		full_proxy=proxy.host+":"+proxy.port
		print(full_proxy)
		PROXY = full_proxy
		webdriver.DesiredCapabilities.CHROME['proxy']={
			"httpProxy":PROXY,
			"ftpProxy":PROXY,
			"sslProxy":PROXY,
			"proxyType":"MANUAL",
		}

		browser_num = randrange(0, len(browser_list))
		browser_list[browser_num].refresh()
		print("Browser number", browser_num, " has been refreshed")
		time.sleep(refresh_time)
	a += 1	