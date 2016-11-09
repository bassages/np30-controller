Install:
Copy file np30-controller to /etc/init.d and make it executable:
sudo chmod +x np30-controller
Execute:
sudo update-rc.d np30-controller defaults enable

View service status:
sudo systemctl status np30-controller